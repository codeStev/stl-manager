// src/utils/groupModelsByArtist.ts
export interface ArtistSummary {
  id: number;
  name: string;
  // add other fields if you need them in the UI
}

export interface ModelDetailed {
  id: number;
  name: string;
  artist: ArtistSummary | null;
  // keep other fields as-is; not required for grouping
}

export interface ArtistGroup {
  artistKey: string; // stringified id or 'null'
  artist: ArtistSummary | null;
  models: ModelDetailed[];
}

export function groupModelsByArtist(models: ModelDetailed[]): ArtistGroup[] {
  const map = new Map<string, ArtistGroup>();

  for (const m of models) {
    const key = m.artist?.id != null ? String(m.artist.id) : 'null';
    let group = map.get(key);
    if (!group) {
      group = { artistKey: key, artist: m.artist ?? null, models: [] };
      map.set(key, group);
    }
    group.models.push(m);
  }

  // Sort models per artist by name (optional)
  for (const g of map.values()) {
    g.models.sort((a, b) => a.name.localeCompare(b.name));
  }

  // Sort groups by artist name, with “No artist” last (tweak as needed)
  const groups = Array.from(map.values());
  groups.sort((a, b) => {
    const an = a.artist?.name ?? 'No artist';
    const bn = b.artist?.name ?? 'No artist';
    if (a.artist && !b.artist) return -1;
    if (!a.artist && b.artist) return 1;
    return an.localeCompare(bn);
  });

  return groups;
}
