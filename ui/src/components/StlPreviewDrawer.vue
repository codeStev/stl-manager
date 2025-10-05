<!-- src/components/StlPreviewDrawer.vue -->
<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from 'vue'
import * as THREE from 'three'
import {STLLoader} from 'three/examples/jsm/loaders/STLLoader.js'
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js'
import axios from 'axios'

type Props = {
  fileId: number | null
  // Build the binary download URL for the STL file
  urlBuilder?: (id: number) => string
  // v-model:open
  open?: boolean
  // Drawer width in px
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

function initThree() {
  if (!canvasHost.value) return

  const div = canvasHost.value
  const rect = div.getBoundingClientRect()

  renderer = new THREE.WebGLRenderer({antialias: true, alpha: true})
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(rect.width, rect.height)
  div.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  // Lights
  scene.add(new THREE.AmbientLight(0xffffff, 1.0))
  const dir = new THREE.DirectionalLight(0xffffff, 1.0)
  dir.position.set(2, 2, 2)
  scene.add(dir)

  camera = new THREE.PerspectiveCamera(50, rect.width / rect.height, 0.1, 5000)
  camera.position.set(0, 0, 200)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08

  // Optional grid
  const grid = new THREE.GridHelper(400, 40, 0x444444, 0x222222)
  grid.position.y = -80
  scene.add(grid)

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

function disposeThree() {
  if (ro && canvasHost.value) ro.unobserve(canvasHost.value)
  ro = null

  if (controls) {
    controls.dispose()
    controls = null
  }

  if (mesh) {
    mesh.geometry.dispose()
    ;(mesh.material as THREE.Material).dispose()
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
}

function animate() {
  if (!renderer || !scene || !camera) return
  rafId = requestAnimationFrame(animate)
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
      color: 0x9eb6ff,
      metalness: 0.05,
      roughness: 0.8,
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
