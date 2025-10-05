<!-- src/components/StlPreviewDrawer.vue -->
<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from 'vue'
import * as THREE from 'three'
import {STLLoader} from 'three/examples/jsm/loaders/STLLoader.js'
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js'
import axios from 'axios'

type Props = {
  fileId: number | null
  urlBuilder?: (id: number) => string
  open?: boolean
  width?: number
}

const props = withDefaults(defineProps<Props>(), {
  urlBuilder: (id: number) => `/api/file/${id}/content`,
  open: false,
  width: 460,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const isOpen = ref<boolean>(props.open)
watch(() => props.open, v => (isOpen.value = !!v))
watch(isOpen, v => emit('update:open', v))

const visible = computed(() => props.fileId != null)

const canvasHost = ref<HTMLDivElement | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let controls: OrbitControls | null = null
let mesh: THREE.Mesh | null = null
let ro: ResizeObserver | null = null
let rafId = 0

// Object-rotation state
let isRotating = false
let activePointerId: number | null = null
const lastPointer = new THREE.Vector2()
const rotateSpeed = 0.005 // radians per pixel; tweak to taste

// Track the last mesh to which the detail look was applied
let lastMaterialTarget: THREE.Mesh | null = null

function initThree() {
  if (!canvasHost.value) return

  const div = canvasHost.value
  const rect = div.getBoundingClientRect()

  renderer = new THREE.WebGLRenderer({antialias: true, alpha: true})
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(rect.width, rect.height)
  // Color management for preserving detail
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.0
  div.appendChild(renderer!.domElement)

  scene = new THREE.Scene()
  // Lights
  scene.add(new THREE.AmbientLight(0xffffff, 0.7))
  const dir1 = new THREE.DirectionalLight(0xffffff, 0.8)
  dir1.position.set(2, 2, 2)
  scene.add(dir1)
  const dir2 = new THREE.DirectionalLight(0xffffff, 0.4)
  dir2.position.set(-2, -2, -2)
  scene.add(dir2)
  const dir3 = new THREE.DirectionalLight(0xffffff, 0.3)
  dir3.position.set(0, 5, 0)
  scene.add(dir3)

  camera = new THREE.PerspectiveCamera(50, rect.width / rect.height, 0.1, 5000)
  camera.position.set(0, 0, 200)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  // Disable camera rotation; keep zoom and pan
  controls.enableRotate = false
  controls.enableZoom = true
  controls.enablePan = true
  controls.mouseButtons = {
    LEFT: THREE.MOUSE.ROTATE, // disabled by enableRotate=false
    MIDDLE: THREE.MOUSE.DOLLY,
    RIGHT: THREE.MOUSE.PAN,
  }
  controls.touches = {
    ONE: THREE.TOUCH.ROTATE, // disabled by enableRotate=false
    TWO: THREE.TOUCH.DOLLY_PAN,
  }

  // Pointer-based object rotation
  const dom = renderer.domElement
  dom.addEventListener('pointerdown', onPointerDown)
  dom.addEventListener('pointermove', onPointerMove)
  dom.addEventListener('pointerup', onPointerUpCancel)
  dom.addEventListener('pointercancel', onPointerUpCancel)
  dom.addEventListener('lostpointercapture', onPointerUpCancel)

  animate()

  ro = new ResizeObserver(() => {
    if (!renderer || !camera || !canvasHost.value) return
    const r = canvasHost.value.getBoundingClientRect()
    renderer.setSize(r.width, r.height)
    camera.aspect = r.width / r.height
    camera.updateProjectionMatrix()
  })
  ro.observe(div)
}

// Best-detail PBR material
function createDetailMaterial(): THREE.MeshStandardMaterial {
  return new THREE.MeshStandardMaterial({
    color: 0xB0B0B0,   // neutral mid-gray
    metalness: 0.05,   // low metalness for plastic/ceramic look
    roughness: 0.4,    // mid roughness for strong shape cues
    flatShading: false // set true if you want faceted look
  })
}

// Subtle edge overlay to emphasize silhouette/creases
function addEdgeOverlay(target: THREE.Mesh) {
  const geom = target.geometry as THREE.BufferGeometry
  const edgesGeom = new THREE.EdgesGeometry(geom, 20) // threshold angle (degrees)
  const lines = new THREE.LineSegments(
    edgesGeom,
    new THREE.LineBasicMaterial({
      color: 0x000000,
      transparent: true,
      opacity: 0.25
    })
  )
  lines.name = 'edgesOverlay'
  target.add(lines)
}

// Apply material + ensure normals, once per mesh
function applyDetailLookToMesh(m: THREE.Mesh) {
  const g = m.geometry as THREE.BufferGeometry
  if (!g.attributes.normal) g.computeVertexNormals()
  g.normalizeNormals()
  m.material = createDetailMaterial()
  // Add at most one overlay
  if (!m.getObjectByName('edgesOverlay')) addEdgeOverlay(m)
}

function onPointerDown(e: PointerEvent) {
  if (!renderer || !mesh) return
  const isPrimaryMouse = e.pointerType === 'mouse' && e.button === 0
  const isTouch = e.pointerType === 'touch'
  if (!isPrimaryMouse && !isTouch) return

  isRotating = true
  activePointerId = e.pointerId
  lastPointer.set(e.clientX, e.clientY)
  renderer.domElement.setPointerCapture(e.pointerId)
  renderer.domElement.style.cursor = 'grabbing'
  e.preventDefault()
}

function onPointerMove(e: PointerEvent) {
  if (!isRotating || e.pointerId !== activePointerId) return
  if (!mesh || !camera) return

  const dx = e.clientX - lastPointer.x
  const dy = e.clientY - lastPointer.y
  lastPointer.set(e.clientX, e.clientY)

  const axisY = new THREE.Vector3(0, 1, 0)
  const axisRight = new THREE.Vector3(1, 0, 0).applyQuaternion(camera.quaternion).normalize()

  const qx = new THREE.Quaternion().setFromAxisAngle(axisRight, dy * rotateSpeed)
  const qy = new THREE.Quaternion().setFromAxisAngle(axisY, dx * rotateSpeed)

  mesh.quaternion.premultiply(qy)
  mesh.quaternion.premultiply(qx)

  e.preventDefault()
}

function onPointerUpCancel(e: PointerEvent) {
  if (e.pointerId !== activePointerId) return
  isRotating = false
  activePointerId = null
  if (renderer) {
    try { renderer.domElement.releasePointerCapture(e.pointerId) } catch {}
    renderer.domElement.style.cursor = 'default'
  }
}

function disposeThree() {
  if (ro && canvasHost.value) ro.unobserve(canvasHost.value)
  ro = null

  if (renderer) {
    const dom = renderer.domElement
    dom.removeEventListener('pointerdown', onPointerDown)
    dom.removeEventListener('pointermove', onPointerMove)
    dom.removeEventListener('pointerup', onPointerUpCancel)
    dom.removeEventListener('pointercancel', onPointerUpCancel)
    dom.removeEventListener('lostpointercapture', onPointerUpCancel)
  }

  if (controls) {
    controls.dispose()
    controls = null
  }

  if (mesh) {
    // Dispose mesh and any overlays (like edges) safely
    mesh.traverse((obj: THREE.Object3D) => {
      const anyObj = obj as any
      if (anyObj.geometry && typeof anyObj.geometry.dispose === 'function') {
        anyObj.geometry.dispose()
      }
      if (anyObj.material) {
        if (Array.isArray(anyObj.material)) {
          anyObj.material.forEach((m: THREE.Material) => m?.dispose?.())
        } else {
          (anyObj.material as THREE.Material)?.dispose?.()
        }
      }
    })
    scene?.remove(mesh)
    mesh = null
  }

  cancelAnimationFrame(rafId)

  if (renderer) {
    renderer.dispose()
    if (renderer.domElement.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement)
    }
    renderer = null
  }

  if (scene) {
    scene.clear()
    scene = null
  }

  camera = null
  lastMaterialTarget = null
}

function animate() {
  if (!renderer || !scene || !camera) return
  rafId = requestAnimationFrame(animate)

  // Auto-apply the detail look whenever a new mesh is set
  if (mesh && mesh !== lastMaterialTarget) {
    applyDetailLookToMesh(mesh)
    lastMaterialTarget = mesh
  }

  controls?.update()
  renderer.render(scene, camera)
}


async function loadStl(id: number) {
  if (!scene) initThree()
  if (!scene) return

  // Remove previous mesh
  if (mesh) {
    mesh.geometry.dispose()
    ;(mesh.material as THREE.Material).dispose()
    scene.remove(mesh)
    mesh = null
  }

  loading.value = true
  error.value = null

  try {
    const url = props.urlBuilder(id)
    // Download as binary via Axios (ensures credentials/headers if configured)
    const resp = await axios.get<ArrayBuffer>(url, {responseType: 'arraybuffer'})
    const buffer = resp.data

    const loader = new STLLoader()
    const geometry = loader.parse(buffer)

    geometry.computeBoundingBox()
    geometry.computeVertexNormals()

    const material = new THREE.MeshStandardMaterial({
      color: 0xAEB7D4,
      metalness: 0.2,
      roughness: 0.6,
    })

    mesh = new THREE.Mesh(geometry, material)

    // Center geometry at origin
    const bb = geometry.boundingBox!
    const center = new THREE.Vector3()
    bb.getCenter(center)
    geometry.translate(-center.x, -center.y, -center.z)

    scene!.add(mesh)
    fitViewToObject()
  } catch (e: any) {
    error.value = e?.message ?? 'Failed to load STL'
  } finally {
    loading.value = false
  }
}

function fitViewToObject() {
  if (!mesh || !camera || !controls) return
  const box = new THREE.Box3().setFromObject(mesh)
  const size = box.getSize(new THREE.Vector3())
  const maxDim = Math.max(size.x, size.y, size.z)
  const fov = (camera!.fov * Math.PI) / 180
  let cameraZ = Math.abs(maxDim / Math.tan(fov / 2))
  cameraZ *= 1.2 // padding
  camera!.position.set(0, 0, cameraZ)
  controls!.target.set(0, 0, 0)
  controls!.update()
}

watch(
  () => props.fileId,
  async id => {
    if (id == null) {
      isOpen.value = false
      disposeThree()
      return
    }
    // Lazy-init and open drawer
    if (!isOpen.value) isOpen.value = true
    if (!renderer) initThree()
    await loadStl(id)
  }
)

watch(
  () => isOpen.value,
  open => {
    if (!open) {
      // Free resources when closed
      disposeThree()
    } else if (open && props.fileId != null && !renderer) {
      // Re-opened with a file selected
      initThree()
      loadStl(props.fileId)
    }
  }
)

onBeforeUnmount(disposeThree)
</script>

<template>
  <!-- Pull-out handle (visible when a file is selected and drawer is closed) -->
  <div v-if="visible && !isOpen" class="stl-handle">
    <v-btn density="comfortable" color="primary" @click="isOpen = true" icon title="Open 3D Preview">
      <v-icon>mdi-cube-outline</v-icon>
    </v-btn>
  </div>

  <!-- Drawer -->
  <v-navigation-drawer
    v-model="isOpen"
    location="right"
    :width="width"
    temporary
    elevation="8"
    scrim
  >
    <v-toolbar density="compact" color="transparent" elevation="0">
      <v-toolbar-title>3D Preview</v-toolbar-title>
      <v-spacer />
      <v-btn icon @click="fitViewToObject" :disabled="!visible || loading">
        <v-icon>mdi-target</v-icon>
      </v-btn>
      <v-btn icon @click="isOpen = false">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-toolbar>

    <v-divider />

    <div class="stl-canvas-wrapper">
      <div ref="canvasHost" class="stl-canvas"></div>

      <div v-if="loading" class="stl-overlay">
        <v-progress-circular indeterminate color="primary" />
      </div>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        class="ma-4"
        density="comfortable"
      >
        {{ error }}
      </v-alert>

      <div v-if="!loading && !error && !visible" class="stl-empty">
        Select a file to preview.
      </div>
    </div>
  </v-navigation-drawer>
</template>

<style scoped>
.stl-handle {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2050;
}
.stl-canvas-wrapper {
  position: relative;
  height: calc(100vh - 64px);
  /* adjust depending on your app header height */
}
.stl-canvas {
  position: absolute;
  inset: 0;
}
.stl-overlay {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  pointer-events: none;
}
.stl-empty {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: rgba(0,0,0,0.5);
}
</style>
