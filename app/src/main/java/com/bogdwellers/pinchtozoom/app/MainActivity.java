package com.bogdwellers.pinchtozoom.app;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int PICK_IMAGE = 1;
    public static final String DEFAULT_IMAGES_FOLDER = "default_images";
    public static final String PICKED_IMAGES = "picked_images";
    private static final BitmapFactory.Options BITMAP_FACTORY_OPTIONS;
    static {
        BITMAP_FACTORY_OPTIONS = new BitmapFactory.Options();
        BITMAP_FACTORY_OPTIONS.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    private ViewPager viewPager;
    private ImageViewPagerAdapter imageViewPagerAdapter;
    private ArrayList<Uri> pickedImageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Drawable> drawables = new ArrayList<>();
        addDefaultImages(drawables);

        imageViewPagerAdapter = new ImageViewPagerAdapter(drawables);
        viewPager = findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(imageViewPagerAdapter);

        if(savedInstanceState != null) {
            pickedImageUris = savedInstanceState.getParcelableArrayList(PICKED_IMAGES);
        }

        if(pickedImageUris != null) {
            for(Uri uri: pickedImageUris) {
                try {
                    addDrawableByUri(uri);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found", e);
                }
            }
        } else {
            pickedImageUris = new ArrayList<>();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(R.id.add_photo == id) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.add_photo)), PICK_IMAGE);
        } else if(R.id.clear == id) {
            List<Drawable> drawables = imageViewPagerAdapter.drawables;
            drawables.clear();
            addDefaultImages(drawables);
            imageViewPagerAdapter.notifyDataSetChanged();
            pickedImageUris.clear();
        } else if(R.id.info == id) {
            DialogFragment infoDialogFragment = new InfoDialogFragment();
            infoDialogFragment.show(getFragmentManager(), "info");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE) {
            if(data != null) {
                Uri uri = data.getData();
                Log.d(TAG, "Picked image: " + String.valueOf(uri));
                if (uri != null) {
                    try {
                        addDrawableByUri(uri);

                        // Scroll to the end of list
                        viewPager.setCurrentItem(imageViewPagerAdapter.getCount() - 1);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found", e);
                    } finally {
                        pickedImageUris.add(uri);
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PICKED_IMAGES, pickedImageUris);
    }

    /**
     *
     * @param drawables
     */
    private void addDefaultImages(List<Drawable> drawables) {
        // Note: Images are stored as assets instead of as resources
        // This because content should be in its raw format as opposed to UI elements
        // and to have more control over the decoding of image files

        AssetManager assets = getAssets();
        Resources resources = getResources();
        try {
            List<String> images = Arrays.asList(assets.list(DEFAULT_IMAGES_FOLDER));
            Collections.sort(images);
            for(String image: images) {
                InputStream is = null;
                try {
                    is = assets.open(DEFAULT_IMAGES_FOLDER + "/" + image);
                    Bitmap bitmap = BitmapFactory.decodeStream(is, null, BITMAP_FACTORY_OPTIONS);
                    drawables.add(new BitmapDrawable(resources, bitmap));
                } finally {
                    if(is != null) {
                        try {
                            is.close();
                        } catch(IOException ignored) {
                        }
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param uri
     */
    private void addDrawableByUri(Uri uri) throws FileNotFoundException {
        InputStream is = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, BITMAP_FACTORY_OPTIONS);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

        // Add drawable to end of list
        imageViewPagerAdapter.drawables.add(drawable);
        imageViewPagerAdapter.notifyDataSetChanged();
    }

    /**
     *
     */
    private static class ImageViewPagerAdapter extends PagerAdapter {

        private List<Drawable> drawables;

        public ImageViewPagerAdapter(List<Drawable> drawables) {
            this.drawables = drawables;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = container.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.page_image, null);
            container.addView(view);

            ImageView imageView = view.findViewById(R.id.image);
            imageView.setImageDrawable(drawables.get(position));

            ImageMatrixTouchHandler imageMatrixTouchHandler = new ImageMatrixTouchHandler(context);
            imageView.setOnTouchListener(imageMatrixTouchHandler);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;

            ImageView imageView = view.findViewById(R.id.image);
            imageView.setImageResource(0);

            container.removeView(view);
        }

        @Override
        public int getCount() {
            return drawables.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition (Object object) {
            return POSITION_NONE;
        }
    }
}
