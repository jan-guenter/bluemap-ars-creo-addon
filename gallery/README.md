# Ars Creo comparison gallery

This gallery places `ars_creo:starbuncle_wheel` in all six `facing` states and
one `minecraft:stone` control. The add-on uses the wheel's installed geometry
and texture in a fixed base pose. Live Create speed, the gold-block RPM bonus,
and GeckoLib animation phase are intentionally excluded.

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
