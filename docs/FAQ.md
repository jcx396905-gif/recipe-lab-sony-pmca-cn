# FAQ

The questions people actually ask. For the step-by-step install, the recipe list and the undo instructions, see the
[README](../README.md).

---

## Will this touch my RAW files?

No. Recipe Lab sets the same camera settings you could set by hand in the menus, and the camera treats them the way
it always has: the look is baked into the **JPEG**, the RAW stays a RAW. Shoot RAW+JPEG and you get a processed JPEG
next to an untouched raw file, exactly as before.

The one catch is the recipes marked **PE**, which are built on a Picture Effect. The A6000 refuses to apply a Picture
Effect unless Quality is a JPEG setting — with RAW or RAW+JPEG selected it silently drops the effect. So when storing
one of those would change your Quality, the app asks first:

```
Quality: RAW+JPG → JPG Fine — JPEG is needed to apply this recipe
```

*Cancel* stores nothing and leaves your Quality alone. Pick a Creative Style recipe instead and you keep shooting RAW.

Going back is just as plain: every Creative Style recipe uses whatever Quality the camera is set to, so switch Quality
back to RAW (`QUALITY` chip in the app, or the normal menu) and RAW files come back. Nothing about the switch is
one-way.

## Can this damage or brick my camera?

Installing an app is the mechanism Sony itself shipped: the camera has an app menu, and `Sony-PMCA-RE` talks to it the
same way Sony's app store did before it closed. No firmware is replaced, nothing is unlocked, no "jailbreak".

Recipe Lab then writes only values the menus can already write — Creative Style and its sliders, white balance,
exposure bias, Picture Effect, DRO, Quality — plus one hidden colour-matrix switch the camera has but never lists.
Any of it can be undone from the camera's own
[`Setup → Setting Reset → Camera Settings Reset`](../README.md#uninstalling).

The honest caveat is **other bodies**. The setting IDs were reverse engineered on an A6000, and the same value can
live in a different slot on a different model — so on an untested body a recipe could land somewhere unintended.
That is why the [compatibility table](../README.md#compatibility) only marks a camera ✅ once someone has actually
run it. If your body is a ❔, expect to check the app's chips against your menus before storing anything.

## Do I need Wi-Fi, a Sony account, or the app store?

No. Sony's PlayMemories app store closed in 2021 and none of this goes near it. The app arrives over the USB cable
from your computer.

## Does the look stay with the app closed?

Yes — that is the whole point. Storing a recipe writes it into the camera's settings, so it applies in every mode,
photo and video, with Recipe Lab closed and after a power cycle. What the app shows *before* you store is only a live
preview, and that disappears when you leave the app.

## Will it work on my camera?

If your camera can run PlayMemories camera apps, it should install. Whether the stored values land correctly is the
open question, body by body — see [Compatibility](../README.md#compatibility), which lists every app-capable model,
which ones people have confirmed, and which Sony bodies cannot run apps at all.

If you try one, please file a
[compatibility report](https://github.com/voxivoid/recipe-lab-sony-pmca/issues/new?template=compatibility_report.yml).

## My camera is newer than that — is there a version for it?

There cannot be. Sony's last app-capable bodies are from late 2016, the A6500 and the A99 II. Everything since —
A6100, A6400, A6600, A6700, A7 III onwards, A9, A1, the ZV and FX cameras, RX100 VA and later — has signed firmware
and no `MENU → Application`, so nothing can be installed on it by anyone.

Those cameras do have Picture Profiles and Creative Looks in the menus, which is the feature Recipe Lab exists to make
up for on the A6000.

## Can I make my own recipes and save them?

Not today. The 77 recipes are built into the app, and you can adjust a recipe's values before storing it — the chips
in the app row — but there is no way to name and keep your own. It is the most requested feature and it is being
thought about; nothing is promised.

## Can it use LUTs, like JPEG.CAM does?

No, and not by accident. Recipe Lab never processes an image — it picks camera settings and gets out of the way,
which is why the look survives with the app closed and why it applies to video too. The A6000's settings store has no
LUT, no tone curve and no gamma to write, so there is nothing to point a LUT at.

Applying a LUT would mean the app taking the picture itself and re-processing the file, which is a different kind of
tool — [JPEG.CAM](https://www.jpeg.cam) does exactly that, and is worth a look if that is what you want. It cannot be
real time, and it cannot touch video.

## What about grain, halation, bloom, or light leaks?

Same wall. Those are image processing, and this app does no image processing — only what the camera's own hardware
does while it writes the JPEG. Film grain in particular is not a setting the A6000 has.

## Can I map a custom button to launch the app?

No. The camera's custom-key list is fixed in firmware and apps cannot add themselves to it, so the app opens from
`MENU → Application → Application List`. Changing that would mean custom firmware, which this project does not do.

## Can I get S-Log, V-Log or a flat profile?

Not on this body. The A6000 has no Picture Profile menu and cannot store a gamma curve — the log profiles simply are
not there to switch on. Sony's camcorder *Cinematone* gamma does exist inside the firmware, but the A6000's camera
layer neither lists nor accepts it. The flattest thing available is a low-contrast Creative Style recipe, which is
not the same thing.

## Are there sample photos of each recipe?

Yes — **[docs/SAMPLES.md](SAMPLES.md)**. All 77 recipes on one subject, in one light, at one fixed exposure
(1/30, f/4.5, ISO 250), straight out of the camera and not edited, downscaled to 900 px so the page stays light.
Nothing that touches colour was done to them.

The scene has no skin tone, foliage or sky, so it judges the portrait-leaning recipes worst; for those the app's own
live preview is still the better sample — turn the wheel and the image on the screen is what the camera will write.

## Something went wrong

The [Troubleshooting table](../README.md#troubleshooting) covers the common ones. Install and USB problems belong to
[Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE/issues), which is what puts apps on the camera; anything about
the recipes or the app itself belongs [here](https://github.com/voxivoid/recipe-lab-sony-pmca/issues).
