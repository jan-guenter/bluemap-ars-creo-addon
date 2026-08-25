# Ars Creo comparison gallery

This gallery places `ars_creo:starbuncle_wheel` in all six `facing` states and
one `minecraft:stone` control. The add-on uses the installed geometry, `run`
animation, and texture in a four-pose, 11-tick loop. Live Create speed and the
gold-block RPM bonus are intentionally excluded, so the wheel stays static.

Replace `cases.py` with the smallest real defect fixture and stock controls,
then keep the stable commands:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/ars-creo-gallery.zip
```

Keep gallery generation deterministic, bounded, and free of candidate assets
or captured meshes.
