import type {Library} from "@/types/external/LibraryTypes.ts";
import type {Artist} from "@/types/external/ArtistTypes.ts";
import type {StlFileBaseInfo} from "@/types/external/StlFileTypes.ts";

export interface ModelDetails {
  id : number,
  name : string,
  library : Library,
  artist :Artist,
  variants : ModelVariant[],
  thumbnail : string | null,
  previews : string[],
}

export interface ModelVariant {
  id : number,
  name : string,
  stlFiles : StlFileBaseInfo[]
}
/**
 * {
 *     "id": 2,
 *     "name": "IT Clown",
 *     "library": {
 *       "library_id": 1,
 *       "library_name": "stltestlib2",
 *       "library_path": "/home/shohnke/stltestlib"
 *     },
 *     "artist": {
 *       "id": 1,
 *       "name": "NomNom Figures"
 *     },
 *     "thumbnail": null,
 *     "previews": [],
 *     "stlFiles": [
 *       {
 *         "id": 115,
 *         "fileName": "Face 1"
 *       },
 *       {
 *         "id": 93,
 *         "fileName": "Face 2"
 *       },
 *       {
 *         "id": 67,
 *         "fileName": "Face 3"
 *       },
 *       {
 *         "id": 165,
 *         "fileName": "NOM_Face 1_Supported"
 *       },
 *       {
 *         "id": 151,
 *         "fileName": "NOM_Face 2_Supported"
 *       },
 *       {
 *         "id": 133,
 *         "fileName": "NOM_Face 3_Supported"
 *       }
 *     ],
 *     "variants": [
 *       {
 *         "id": 3,
 *         "name": "Presupport"
 *       },
 *       {
 *         "id": 4,
 *         "name": "STL"
 *       }
 *     ]
 *   },
 */
