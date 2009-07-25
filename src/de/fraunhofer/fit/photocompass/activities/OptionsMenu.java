package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import de.fraunhofer.fit.photocompass.R;

/**
 * This class provides static methods for all activities to populate their options menu and to handle the menu item
 * selections.
 */
public final class OptionsMenu {
    
    // menu items
    private static final int MENU_OPEN_CAMERA = 0;
    private static final int MENU_QUIT = 1;
    
    // activity returns
    static final int CAMERA_RETURN = 0;
    
    
    /**
     * Populates the options menu. Call this from {@link Activity#onCreateOptionsMenu(Menu)}.
     * 
     * @param menu {@link Menu} object to populate.
     * @return Populated {@link Menu}.
     */
    public static Menu populateMenu(final Menu menu) {

        System.gc(); // good point to run the GC
        menu.add(0, MENU_OPEN_CAMERA, 0, "Take New Photo").setIcon(R.drawable.menu_camera);
        menu.add(0, MENU_QUIT, 0, "Quit PhotoCompass").setIcon(R.drawable.menu_quit);
        return menu;
    }
    

    /**
     * Handles a menu item selection. Call this from {@link Activity#onOptionsItemSelected(MenuItem)}
     * 
     * @param item Selected {@link MenuItem}.
     * @param activity Calling {@link Activity}.
     * @return <code>true</code> if the selection is handled, or <code>false</code> if the selection is not handled.
     */
    public static boolean handleMenuItemSelection(final MenuItem item, final Activity activity) {

        switch (item.getItemId()) {
            case MENU_OPEN_CAMERA:
//		    	ContentValues values = new ContentValues();
//	               values.put(Media.TITLE, "Image");
//	                values.put(Media.DESCRIPTION, "Image capture by camera");
//                Uri uri = activity.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
//		    	Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//		        imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // 0 = low quali, 1 = high quali
//		        activity.startActivityForResult(imageCaptureIntent, CAMERA_RETURN);
                activity.startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), CAMERA_RETURN);
                return true;
            case MENU_QUIT:
//		        activity.finish();
                activity.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
                return true;
        }
        return false;
    }
}
