# Changes against AOSP WallpaperPicker (android-10.0.0_r9)

Tj (2026-11-06): "Attached is what the wallpaper picker should look like. The reference is the screenshot without the my photos button ... There should be no my photos option ... You may be able to find the source online and simply remove the my photos choice."

- `WallpaperPickerActivity.init()`: the tile for the Gallery ("My photos", `PickImageInfo`) is NOT added, so the strip starts with the first wallpaper (the reference look). `tileinfo/PickImageInfo.java`, `layout/wallpaper_picker_image_picker_item.xml` and `drawable*/ic_images.png` were deleted.
- `WallpaperPickerActivity.getWallpaperArrayResourceId()`: AOSP's own `wallpapers` array is empty (each device build fills it through an overlay of the picker app), so the wallpapers are read from the picker app that is INSTALLED on the tablet (`com.tj.launcher.WallpaperPickerChoice.bundledWallpapers`): exactly the stock set. The empty array of this module (`res/values-nodpi/wallpapers.xml`) is only the fallback.
- `tileinfo/ThirdPartyWallpaperInfo.getAll()` returns an empty list: no tile for any other app (Tj: "doesn't offer another app").
- Every `R` is `com.android.launcher3.R` (the app's resource namespace); `android.support.v4.util.Pools` -> `androidx.core.util.Pools`.
- Resources: only the default (English) values, no translations, no launcher icon, no `ic_images`; names do not collide with Launcher3's (checked 2026-11-06).
- Manifest (`app/src/main/AndroidManifest.xml`): `WallpaperPickerActivity` is declared with NO intent filter, `exported=false`, `excludeFromRecents=true`; only the long-press entry (`OptionsPopupView.startWallpaperPicker`) starts it. `WallpaperCropActivity` (its base class) is not declared: nothing starts it.
