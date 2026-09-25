# Sample frames

Every one of the 77 recipes, on the same subject, in the same light, straight out of the camera.

One frame per recipe, shot in one run by the app's own [sample run](DEVELOPMENT.md#sample-run): it applies recipe *n*,
waits for the preview pipeline, fires the shutter, moves on. Nothing here is edited. The frames are downscaled to
900 px wide (Lanczos, JPEG q82, no chroma subsampling, EXIF kept) — nothing that touches colour was done to them.
These copies are all that is published: the full-size frames are 500 MB, which is not worth carrying in a repository
whose whole point is a 200 KB app.

**How they were shot.** ILCE-6001, firmware v3.21, tripod, indoors in the evening, manual exposure fixed across all
77 frames: **1/30, f/4.5, ISO 250, 27 mm**. White balance is whatever the recipe says — auto for 69 of them, a fixed
colour temperature for the 8 that specify one. So every difference below is the recipe, not the light and not the
exposure.

**What this scene does and does not show.** Strong orange, several greens and a teal against a large neutral grey
floor and a white wall — good for judging saturation, contrast and colour cast, and for seeing which recipes go
fully monochrome. It has **no real skin tone, no foliage and no sky**, so the portrait-leaning recipes (Portra,
Astia, Pro Neg, Canon Portrait) are the ones this scene judges worst. And it is **indoor light**: the eight recipes
that fix a colour temperature (Acros +Ye / +R / +G, Vision3 500T, Asteroid City, Cinestill 50D / 800T, Classic Cinema)
render relative to the room, not to daylight — 5600K comes out amber here, 3200K nearly neutral. Judge those eight
in daylight, not from this page.

Values are read as in the app: *style  sat/con*, then the extras — `MTX` alternate colour matrix, `±EV`, `DRO`,
kelvin, `A`/`B` amber-blue and `G`/`M` green-magenta fine tune. Effect recipes show the effect name instead of a style.

---

**Brands**

- [Sony](#sony) · 8
- [Fuji Sim](#fuji-sim) · 16
- [Fuji Film](#fuji-film) · 5
- [Kodak](#kodak) · 14
- [Cine](#cine) · 4
- [Ricoh GR](#ricoh-gr) · 8
- [Leica](#leica) · 4
- [Hasselblad](#hasselblad) · 1
- [Canon / Nikon](#canon--nikon) · 5
- [Pana / Olympus](#pana--olympus) · 4
- [Other Stocks](#other-stocks) · 3
- [Ilford](#ilford) · 5
- [Frame list](#frame-list) — which original file is which recipe

---

## Sony

### 01 · FACTORY (ST)

`Standard  0/0`

![FACTORY (ST)](samples/01-factory-st.jpg "FACTORY (ST) — Standard  0/0")

### 02 · Sony PT (portrait)

`Portrait  0/0`

![Sony PT (portrait)](samples/02-sony-pt-portrait.jpg "Sony PT (portrait) — Portrait  0/0")

### 03 · Sony NT (neutral)

`Neutral  0/0`

![Sony NT (neutral)](samples/03-sony-nt-neutral.jpg "Sony NT (neutral) — Neutral  0/0")

### 04 · Sony VV (vivid)

`Vivid  0/0`

![Sony VV (vivid)](samples/04-sony-vv-vivid.jpg "Sony VV (vivid) — Vivid  0/0")

### 05 · Sony VV2

`Vivid  +2/+1  MTX`

![Sony VV2](samples/05-sony-vv2.jpg "Sony VV2 — Vivid  +2/+1  MTX")

### 06 · Sony FL (film-like)

`Neutral  -4/-1  A1`

![Sony FL (film-like)](samples/06-sony-fl-film-like.jpg "Sony FL (film-like) — Neutral  -4/-1  A1")

### 07 · Sony IN (instant)

`Neutral  -4/-3  M1`

![Sony IN (instant)](samples/07-sony-in-instant.jpg "Sony IN (instant) — Neutral  -4/-3  M1")

### 08 · Sony SH (soft high-key)

`High-key blue  +1.0  A1`

![Sony SH (soft high-key)](samples/08-sony-sh-soft-high-key.jpg "Sony SH (soft high-key) — High-key blue  +1.0  A1")


## Fuji Sim

### 09 · Provia

`Standard  +1/0`

![Provia](samples/09-provia.jpg "Provia — Standard  +1/0")

### 10 · Velvia

`Vivid  +5/+2  MTX`

![Velvia](samples/10-velvia.jpg "Velvia — Vivid  +5/+2  MTX")

### 11 · Astia

`Portrait  0/-1  +0.3  A1`

![Astia](samples/11-astia.jpg "Astia — Portrait  0/-1  +0.3  A1")

### 12 · Classic Chrome

`Standard  -1/+2  -0.3  A3  G1`

![Classic Chrome](samples/12-classic-chrome.jpg "Classic Chrome — Standard  -1/+2  -0.3  A3  G1")

### 13 · Classic Negative

`Standard  -3/+3  B1  G1`

![Classic Negative](samples/13-classic-negative.jpg "Classic Negative — Standard  -3/+3  B1  G1")

### 14 · Nostalgic Neg

`Standard  +1/0  A3`

![Nostalgic Neg](samples/14-nostalgic-neg.jpg "Nostalgic Neg — Standard  +1/0  A3")

### 15 · Reala Ace

`Standard  0/+1`

![Reala Ace](samples/15-reala-ace.jpg "Reala Ace — Standard  0/+1")

### 16 · Pro Neg Std

`Portrait  -2/-1`

![Pro Neg Std](samples/16-pro-neg-std.jpg "Pro Neg Std — Portrait  -2/-1")

### 17 · Pro Neg Hi

`Portrait  -2/+1`

![Pro Neg Hi](samples/17-pro-neg-hi.jpg "Pro Neg Hi — Portrait  -2/+1")

### 18 · Eterna

`Neutral  -4/-2  -0.3  DRO Lv3`

![Eterna](samples/18-eterna.jpg "Eterna — Neutral  -4/-2  -0.3  DRO Lv3")

### 19 · Eterna Bleach Bypass

`Neutral  -6/+3  -0.3`

![Eterna Bleach Bypass](samples/19-eterna-bleach-bypass.jpg "Eterna Bleach Bypass — Neutral  -6/+3  -0.3")

### 20 · Acros

`B&W  0/+1`

![Acros](samples/20-acros.jpg "Acros — B&W  0/+1")

### 21 · Acros +Ye (yellow filter)

`B&W  0/+1  4000K`

![Acros +Ye (yellow filter)](samples/21-acros-plus-ye-yellow-filter.jpg "Acros +Ye (yellow filter) — B&W  0/+1  4000K")

### 22 · Acros +R (red filter)

`HC mono  2500K`

![Acros +R (red filter)](samples/22-acros-plus-r-red-filter.jpg "Acros +R (red filter) — HC mono  2500K")

### 23 · Acros +G (green filter)

`B&W  0/+1  5600K  G4`

![Acros +G (green filter)](samples/23-acros-plus-g-green-filter.jpg "Acros +G (green filter) — B&W  0/+1  5600K  G4")

### 24 · Sepia

`Sepia  0/0`

![Sepia](samples/24-sepia.jpg "Sepia — Sepia  0/0")


## Fuji Film

### 25 · Fuji Pro 400H

`Light  -2/-2  +0.7  B1  G1`

![Fuji Pro 400H](samples/25-fuji-pro-400h.jpg "Fuji Pro 400H — Light  -2/-2  +0.7  B1  G1")

### 26 · Fuji Fortia 50

`Vivid  +4/+2  MTX  -0.3  B1`

![Fuji Fortia 50](samples/26-fuji-fortia-50.jpg "Fuji Fortia 50 — Vivid  +4/+2  MTX  -0.3  B1")

### 27 · Fuji Superia 400

`Standard  +1/+1  +0.3  A1  G1`

![Fuji Superia 400](samples/27-fuji-superia-400.jpg "Fuji Superia 400 — Standard  +1/+1  +0.3  A1  G1")

### 28 · Fuji C200

`Standard  0/0  B1  G1`

![Fuji C200](samples/28-fuji-c200.jpg "Fuji C200 — Standard  0/0  B1  G1")

### 29 · Fuji Natura 1600

`Portrait  -2/-2  +0.3  A1`

![Fuji Natura 1600](samples/29-fuji-natura-1600.jpg "Fuji Natura 1600 — Portrait  -2/-2  +0.3  A1")


## Kodak

### 30 · Kodak Portra 160

`Portrait  -3/-2  +0.7  A1  M1`

![Kodak Portra 160](samples/30-kodak-portra-160.jpg "Kodak Portra 160 — Portrait  -3/-2  +0.7  A1  M1")

### 31 · Kodak Portra 400

`Portrait  -1/-1  +0.7  A3  G1`

![Kodak Portra 400](samples/31-kodak-portra-400.jpg "Kodak Portra 400 — Portrait  -1/-1  +0.7  A3  G1")

### 32 · Kodak Portra 800

`Portrait  -2/-2  +0.3  A1  G1`

![Kodak Portra 800](samples/32-kodak-portra-800.jpg "Kodak Portra 800 — Portrait  -2/-2  +0.3  A1  G1")

### 33 · Kodak Gold 200

`Standard  +2/+1  +0.3  A3  G1`

![Kodak Gold 200](samples/33-kodak-gold-200.jpg "Kodak Gold 200 — Standard  +2/+1  +0.3  A3  G1")

### 34 · Kodak Ultra Max 400

`Standard  0/0  +0.3  G1`

![Kodak Ultra Max 400](samples/34-kodak-ultra-max-400.jpg "Kodak Ultra Max 400 — Standard  0/0  +0.3  G1")

### 35 · Kodak Color Plus 200

`Standard  +1/+1  +0.3  A2  G1`

![Kodak Color Plus 200](samples/35-kodak-color-plus-200.jpg "Kodak Color Plus 200 — Standard  +1/+1  +0.3  A2  G1")

### 36 · Kodak Ektar 100

`Standard  +3/+1  -0.3`

![Kodak Ektar 100](samples/36-kodak-ektar-100.jpg "Kodak Ektar 100 — Standard  +3/+1  -0.3")

### 37 · Kodak Ektachrome E100

`Clear  +3/+1  -0.3  B1`

![Kodak Ektachrome E100](samples/37-kodak-ektachrome-e100.jpg "Kodak Ektachrome E100 — Clear  +3/+1  -0.3  B1")

### 38 · Kodachrome 64

`Deep  +3/+2  -0.3  B1`

![Kodachrome 64](samples/38-kodachrome-64.jpg "Kodachrome 64 — Deep  +3/+2  -0.3  B1")

### 39 · Kodak Vision3 500T (daylight)

`Neutral  -1/0  +0.3  DRO Lv3  3200K`

![Kodak Vision3 500T (daylight)](samples/39-kodak-vision3-500t-daylight.jpg "Kodak Vision3 500T (daylight) — Neutral  -1/0  +0.3  DRO Lv3  3200K")

### 40 · Kodak Vision 200T (Asteroid City)

`Neutral  -2/-3  +0.3  DRO Lv3  5000K  A2  G3`

![Kodak Vision 200T (Asteroid City)](samples/40-kodak-vision-200t-asteroid-city.jpg "Kodak Vision 200T (Asteroid City) — Neutral  -2/-3  +0.3  DRO Lv3  5000K  A2  G3")

### 41 · Kodak Tri-X 400

`B&W  0/+2  +0.3`

![Kodak Tri-X 400](samples/41-kodak-tri-x-400.jpg "Kodak Tri-X 400 — B&W  0/+2  +0.3")

### 42 · Kodak T-Max

`B&W  0/+2`

![Kodak T-Max](samples/42-kodak-t-max.jpg "Kodak T-Max — B&W  0/+2")

### 43 · Kodak Tri-X 1600 (pushed)

`HC mono  +0.3`

![Kodak Tri-X 1600 (pushed)](samples/43-kodak-tri-x-1600-pushed.jpg "Kodak Tri-X 1600 (pushed) — HC mono  +0.3")


## Cine

### 44 · Cinestill 50D (Blue Velvet)

`Standard  -2/-1  5500K  B1  M1`

![Cinestill 50D (Blue Velvet)](samples/44-cinestill-50d-blue-velvet.jpg "Cinestill 50D (Blue Velvet) — Standard  -2/-1  5500K  B1  M1")

### 45 · Cinestill 800T

`Neutral  0/0  +0.3  3200K  M1`

![Cinestill 800T](samples/45-cinestill-800t.jpg "Cinestill 800T — Neutral  0/0  +0.3  3200K  M1")

### 46 · Classic Cinema

`Standard  0/-1  DRO Lv3  6000K  A2`

![Classic Cinema](samples/46-classic-cinema.jpg "Classic Cinema — Standard  0/-1  DRO Lv3  6000K  A2")

### 47 · Rec709 Video (flat-ish)

`Neutral  -2/-2  DRO Lv5`

![Rec709 Video (flat-ish)](samples/47-rec709-video-flat-ish.jpg "Rec709 Video (flat-ish) — Neutral  -2/-2  DRO Lv5")


## Ricoh GR

### 48 · GR Positive Film

`Standard  +3/+2  -0.3  A2`

![GR Positive Film](samples/48-gr-positive-film.jpg "GR Positive Film — Standard  +3/+2  -0.3  A2")

### 49 · GR Negative Film

`Neutral  -2/+1  +0.3  B1  G1`

![GR Negative Film](samples/49-gr-negative-film.jpg "GR Negative Film — Neutral  -2/+1  +0.3  B1  G1")

### 50 · GR Bleach Bypass

`Neutral  -6/+3`

![GR Bleach Bypass](samples/50-gr-bleach-bypass.jpg "GR Bleach Bypass — Neutral  -6/+3")

### 51 · GR Retro

`Retro  A3  M1`

![GR Retro](samples/51-gr-retro.jpg "GR Retro — Retro  A3  M1")

### 52 · GR Cross Process

`Vivid  +2/+2  B2  G4`

![GR Cross Process](samples/52-gr-cross-process.jpg "GR Cross Process — Vivid  +2/+2  B2  G4")

### 53 · GR Hi-Contrast B&W

`HC mono`

![GR Hi-Contrast B&W](samples/53-gr-hi-contrast-bandw.jpg "GR Hi-Contrast B&W — HC mono")

### 54 · GR Hard Monotone

`B&W  0/+2`

![GR Hard Monotone](samples/54-gr-hard-monotone.jpg "GR Hard Monotone — B&W  0/+2")

### 55 · GR Soft Monotone

`B&W  0/-2`

![GR Soft Monotone](samples/55-gr-soft-monotone.jpg "GR Soft Monotone — B&W  0/-2")


## Leica

### 56 · Leica Contemporary

`Standard  +1/+1`

![Leica Contemporary](samples/56-leica-contemporary.jpg "Leica Contemporary — Standard  +1/+1")

### 57 · Leica Classic

`Standard  -1/+2  A1`

![Leica Classic](samples/57-leica-classic.jpg "Leica Classic — Standard  -1/+2  A1")

### 58 · Leica Eternal

`Neutral  -3/-1  DRO Lv3  A1`

![Leica Eternal](samples/58-leica-eternal.jpg "Leica Eternal — Neutral  -3/-1  DRO Lv3  A1")

### 59 · Leica Monochrom

`B&W  0/+2`

![Leica Monochrom](samples/59-leica-monochrom.jpg "Leica Monochrom — B&W  0/+2")


## Hasselblad

### 60 · Hasselblad HNCS Natural

`Neutral  -1/-1`

![Hasselblad HNCS Natural](samples/60-hasselblad-hncs-natural.jpg "Hasselblad HNCS Natural — Neutral  -1/-1")


## Canon / Nikon

### 61 · Canon Standard

`Standard  +1/+1  A1  M1`

![Canon Standard](samples/61-canon-standard.jpg "Canon Standard — Standard  +1/+1  A1  M1")

### 62 · Canon Portrait

`Portrait  0/0  A1  M1`

![Canon Portrait](samples/62-canon-portrait.jpg "Canon Portrait — Portrait  0/0  A1  M1")

### 63 · Canon Faithful

`Neutral  0/0`

![Canon Faithful](samples/63-canon-faithful.jpg "Canon Faithful — Neutral  0/0")

### 64 · Nikon Flat

`Neutral  -3/-3  DRO Lv5`

![Nikon Flat](samples/64-nikon-flat.jpg "Nikon Flat — Neutral  -3/-3  DRO Lv5")

### 65 · Nikon Vivid

`Vivid  +1/+1`

![Nikon Vivid](samples/65-nikon-vivid.jpg "Nikon Vivid — Vivid  +1/+1")


## Pana / Olympus

### 66 · Pana L.Monochrome D

`B&W  0/+3`

![Pana L.Monochrome D](samples/66-pana-l-monochrome-d.jpg "Pana L.Monochrome D — B&W  0/+3")

### 67 · Pana L.ClassicNeo

`Neutral  -3/-1  +0.3  A2`

![Pana L.ClassicNeo](samples/67-pana-l-classicneo.jpg "Pana L.ClassicNeo — Neutral  -3/-1  +0.3  A2")

### 68 · Olympus Pop Art

`Vivid  +8/+2  MTX`

![Olympus Pop Art](samples/68-olympus-pop-art.jpg "Olympus Pop Art — Vivid  +8/+2  MTX")

### 69 · Olympus Pale & Light

`Light  -3/-2  +0.7`

![Olympus Pale & Light](samples/69-olympus-pale-and-light.jpg "Olympus Pale & Light — Light  -3/-2  +0.7")


## Other Stocks

### 70 · Agfa Vista 200

`Standard  +2/+1  +0.3  A2  M1`

![Agfa Vista 200](samples/70-agfa-vista-200.jpg "Agfa Vista 200 — Standard  +2/+1  +0.3  A2  M1")

### 71 · Agfa Ultra 100

`Vivid  +6/+1  MTX`

![Agfa Ultra 100](samples/71-agfa-ultra-100.jpg "Agfa Ultra 100 — Vivid  +6/+1  MTX")

### 72 · Polaroid / Instax

`Retro  +0.3  A1  M2`

![Polaroid / Instax](samples/72-polaroid-instax.jpg "Polaroid / Instax — Retro  +0.3  A1  M2")


## Ilford

### 73 · Ilford HP5

`B&W  0/+1  +0.3`

![Ilford HP5](samples/73-ilford-hp5.jpg "Ilford HP5 — B&W  0/+1  +0.3")

### 74 · Ilford FP4

`B&W  0/+1`

![Ilford FP4](samples/74-ilford-fp4.jpg "Ilford FP4 — B&W  0/+1")

### 75 · Ilford Delta 100

`B&W  0/+1`

![Ilford Delta 100](samples/75-ilford-delta-100.jpg "Ilford Delta 100 — B&W  0/+1")

### 76 · Ilford Delta 3200

`B&W  0/+3  +0.7`

![Ilford Delta 3200](samples/76-ilford-delta-3200.jpg "Ilford Delta 3200 — B&W  0/+3  +0.7")

### 77 · Ilford Pan F 50

`B&W  0/+2`

![Ilford Pan F 50](samples/77-ilford-pan-f-50.jpg "Ilford Pan F 50 — B&W  0/+2")


## Frame list

The run shoots in table order and the camera names the files, so the frame number *is* the mapping. This is the same
list the app writes to `samples.txt` in its own files directory, plus the file each frame came out as.

| frame | recipe | brand | values | original |
|---|---|---|---|---|
| 01 | FACTORY (ST) | Sony | `Standard  0/0` | `DSC06567.JPG` |
| 02 | Sony PT (portrait) | Sony | `Portrait  0/0` | `DSC06568.JPG` |
| 03 | Sony NT (neutral) | Sony | `Neutral  0/0` | `DSC06569.JPG` |
| 04 | Sony VV (vivid) | Sony | `Vivid  0/0` | `DSC06570.JPG` |
| 05 | Sony VV2 | Sony | `Vivid  +2/+1  MTX` | `DSC06571.JPG` |
| 06 | Sony FL (film-like) | Sony | `Neutral  -4/-1  A1` | `DSC06572.JPG` |
| 07 | Sony IN (instant) | Sony | `Neutral  -4/-3  M1` | `DSC06573.JPG` |
| 08 | Sony SH (soft high-key) | Sony | `High-key blue  +1.0  A1` | `DSC06574.JPG` |
| 09 | Provia | Fuji Sim | `Standard  +1/0` | `DSC06575.JPG` |
| 10 | Velvia | Fuji Sim | `Vivid  +5/+2  MTX` | `DSC06576.JPG` |
| 11 | Astia | Fuji Sim | `Portrait  0/-1  +0.3  A1` | `DSC06577.JPG` |
| 12 | Classic Chrome | Fuji Sim | `Standard  -1/+2  -0.3  A3  G1` | `DSC06578.JPG` |
| 13 | Classic Negative | Fuji Sim | `Standard  -3/+3  B1  G1` | `DSC06579.JPG` |
| 14 | Nostalgic Neg | Fuji Sim | `Standard  +1/0  A3` | `DSC06580.JPG` |
| 15 | Reala Ace | Fuji Sim | `Standard  0/+1` | `DSC06581.JPG` |
| 16 | Pro Neg Std | Fuji Sim | `Portrait  -2/-1` | `DSC06582.JPG` |
| 17 | Pro Neg Hi | Fuji Sim | `Portrait  -2/+1` | `DSC06583.JPG` |
| 18 | Eterna | Fuji Sim | `Neutral  -4/-2  -0.3  DRO Lv3` | `DSC06584.JPG` |
| 19 | Eterna Bleach Bypass | Fuji Sim | `Neutral  -6/+3  -0.3` | `DSC06585.JPG` |
| 20 | Acros | Fuji Sim | `B&W  0/+1` | `DSC06586.JPG` |
| 21 | Acros +Ye (yellow filter) | Fuji Sim | `B&W  0/+1  4000K` | `DSC06587.JPG` |
| 22 | Acros +R (red filter) | Fuji Sim | `HC mono  2500K` | `DSC06588.JPG` |
| 23 | Acros +G (green filter) | Fuji Sim | `B&W  0/+1  5600K  G4` | `DSC06589.JPG` |
| 24 | Sepia | Fuji Sim | `Sepia  0/0` | `DSC06590.JPG` |
| 25 | Fuji Pro 400H | Fuji Film | `Light  -2/-2  +0.7  B1  G1` | `DSC06591.JPG` |
| 26 | Fuji Fortia 50 | Fuji Film | `Vivid  +4/+2  MTX  -0.3  B1` | `DSC06592.JPG` |
| 27 | Fuji Superia 400 | Fuji Film | `Standard  +1/+1  +0.3  A1  G1` | `DSC06593.JPG` |
| 28 | Fuji C200 | Fuji Film | `Standard  0/0  B1  G1` | `DSC06594.JPG` |
| 29 | Fuji Natura 1600 | Fuji Film | `Portrait  -2/-2  +0.3  A1` | `DSC06595.JPG` |
| 30 | Kodak Portra 160 | Kodak | `Portrait  -3/-2  +0.7  A1  M1` | `DSC06596.JPG` |
| 31 | Kodak Portra 400 | Kodak | `Portrait  -1/-1  +0.7  A3  G1` | `DSC06597.JPG` |
| 32 | Kodak Portra 800 | Kodak | `Portrait  -2/-2  +0.3  A1  G1` | `DSC06598.JPG` |
| 33 | Kodak Gold 200 | Kodak | `Standard  +2/+1  +0.3  A3  G1` | `DSC06599.JPG` |
| 34 | Kodak Ultra Max 400 | Kodak | `Standard  0/0  +0.3  G1` | `DSC06600.JPG` |
| 35 | Kodak Color Plus 200 | Kodak | `Standard  +1/+1  +0.3  A2  G1` | `DSC06601.JPG` |
| 36 | Kodak Ektar 100 | Kodak | `Standard  +3/+1  -0.3` | `DSC06602.JPG` |
| 37 | Kodak Ektachrome E100 | Kodak | `Clear  +3/+1  -0.3  B1` | `DSC06603.JPG` |
| 38 | Kodachrome 64 | Kodak | `Deep  +3/+2  -0.3  B1` | `DSC06604.JPG` |
| 39 | Kodak Vision3 500T (daylight) | Kodak | `Neutral  -1/0  +0.3  DRO Lv3  3200K` | `DSC06605.JPG` |
| 40 | Kodak Vision 200T (Asteroid City) | Kodak | `Neutral  -2/-3  +0.3  DRO Lv3  5000K  A2  G3` | `DSC06606.JPG` |
| 41 | Kodak Tri-X 400 | Kodak | `B&W  0/+2  +0.3` | `DSC06607.JPG` |
| 42 | Kodak T-Max | Kodak | `B&W  0/+2` | `DSC06608.JPG` |
| 43 | Kodak Tri-X 1600 (pushed) | Kodak | `HC mono  +0.3` | `DSC06609.JPG` |
| 44 | Cinestill 50D (Blue Velvet) | Cine | `Standard  -2/-1  5500K  B1  M1` | `DSC06610.JPG` |
| 45 | Cinestill 800T | Cine | `Neutral  0/0  +0.3  3200K  M1` | `DSC06611.JPG` |
| 46 | Classic Cinema | Cine | `Standard  0/-1  DRO Lv3  6000K  A2` | `DSC06612.JPG` |
| 47 | Rec709 Video (flat-ish) | Cine | `Neutral  -2/-2  DRO Lv5` | `DSC06613.JPG` |
| 48 | GR Positive Film | Ricoh GR | `Standard  +3/+2  -0.3  A2` | `DSC06614.JPG` |
| 49 | GR Negative Film | Ricoh GR | `Neutral  -2/+1  +0.3  B1  G1` | `DSC06615.JPG` |
| 50 | GR Bleach Bypass | Ricoh GR | `Neutral  -6/+3` | `DSC06616.JPG` |
| 51 | GR Retro | Ricoh GR | `Retro  A3  M1` | `DSC06617.JPG` |
| 52 | GR Cross Process | Ricoh GR | `Vivid  +2/+2  B2  G4` | `DSC06618.JPG` |
| 53 | GR Hi-Contrast B&W | Ricoh GR | `HC mono` | `DSC06619.JPG` |
| 54 | GR Hard Monotone | Ricoh GR | `B&W  0/+2` | `DSC06620.JPG` |
| 55 | GR Soft Monotone | Ricoh GR | `B&W  0/-2` | `DSC06621.JPG` |
| 56 | Leica Contemporary | Leica | `Standard  +1/+1` | `DSC06622.JPG` |
| 57 | Leica Classic | Leica | `Standard  -1/+2  A1` | `DSC06623.JPG` |
| 58 | Leica Eternal | Leica | `Neutral  -3/-1  DRO Lv3  A1` | `DSC06624.JPG` |
| 59 | Leica Monochrom | Leica | `B&W  0/+2` | `DSC06625.JPG` |
| 60 | Hasselblad HNCS Natural | Hasselblad | `Neutral  -1/-1` | `DSC06626.JPG` |
| 61 | Canon Standard | Canon / Nikon | `Standard  +1/+1  A1  M1` | `DSC06627.JPG` |
| 62 | Canon Portrait | Canon / Nikon | `Portrait  0/0  A1  M1` | `DSC06628.JPG` |
| 63 | Canon Faithful | Canon / Nikon | `Neutral  0/0` | `DSC06629.JPG` |
| 64 | Nikon Flat | Canon / Nikon | `Neutral  -3/-3  DRO Lv5` | `DSC06630.JPG` |
| 65 | Nikon Vivid | Canon / Nikon | `Vivid  +1/+1` | `DSC06631.JPG` |
| 66 | Pana L.Monochrome D | Pana / Olympus | `B&W  0/+3` | `DSC06632.JPG` |
| 67 | Pana L.ClassicNeo | Pana / Olympus | `Neutral  -3/-1  +0.3  A2` | `DSC06633.JPG` |
| 68 | Olympus Pop Art | Pana / Olympus | `Vivid  +8/+2  MTX` | `DSC06634.JPG` |
| 69 | Olympus Pale & Light | Pana / Olympus | `Light  -3/-2  +0.7` | `DSC06635.JPG` |
| 70 | Agfa Vista 200 | Other Stocks | `Standard  +2/+1  +0.3  A2  M1` | `DSC06636.JPG` |
| 71 | Agfa Ultra 100 | Other Stocks | `Vivid  +6/+1  MTX` | `DSC06637.JPG` |
| 72 | Polaroid / Instax | Other Stocks | `Retro  +0.3  A1  M2` | `DSC06638.JPG` |
| 73 | Ilford HP5 | Ilford | `B&W  0/+1  +0.3` | `DSC06639.JPG` |
| 74 | Ilford FP4 | Ilford | `B&W  0/+1` | `DSC06640.JPG` |
| 75 | Ilford Delta 100 | Ilford | `B&W  0/+1` | `DSC06641.JPG` |
| 76 | Ilford Delta 3200 | Ilford | `B&W  0/+3  +0.7` | `DSC06642.JPG` |
| 77 | Ilford Pan F 50 | Ilford | `B&W  0/+2` | `DSC06643.JPG` |
