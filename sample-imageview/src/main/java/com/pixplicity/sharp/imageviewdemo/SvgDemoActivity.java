/*
    Copyright 2011, 2015 Pixplicity, Larva Labs LLC and Google, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Sharp is heavily based on prior work. It was originally forked from
        https://github.com/pents90/svg-android
    And changes from other forks have been consolidated:
        https://github.com/b2renger/svg-android
        https://github.com/mindon/svg-android
        https://github.com/josefpavlik/svg-android
 */

package com.pixplicity.sharp.imageviewdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jsibbold.zoomage.ZoomageView;
import com.pixplicity.sharp.OnSvgElementListener;
import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SharpDrawable;
import com.pixplicity.sharp.SharpPicture;

import java.util.Random;

public class SvgDemoActivity extends AppCompatActivity {

    private ZoomageView mImageView;
    private Button mButton;

    private Sharp mSvg;

    private boolean mRenderBitmap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svg_demo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mImageView = findViewById(R.id.iv_image);
        mButton = findViewById(R.id.bt_button);

        Sharp.setLogLevel(Sharp.LOG_LEVEL_INFO);

        mSvg = Sharp.loadResource(getResources(), R.raw.cartman);
        // If you want to load typefaces from assets:
        //          .withAssets(getAssets());

        // If you want to load an SVG from assets:
        //mSvg = Sharp.loadAsset(getAssets(), "cartman.svg");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadSvg(true);
            }
        });

        reloadSvg(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main, menu);
        menu.findItem(R.id.action_render_bitmap).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mRenderBitmap = !menuItem.isChecked();
                menuItem.setChecked(mRenderBitmap);
                reloadSvg(false);
                return true;
            }
        });
        return true;
    }

    private void reloadSvg(final boolean changeColor) {
        mSvg.setOnElementListener(new OnSvgElementListener() {

            @Override
            public void onSvgStart(@NonNull Canvas canvas,
                                   @Nullable RectF bounds) {
            }

            @Override
            public void onSvgEnd(@NonNull Canvas canvas,
                                 @Nullable RectF bounds) {
            }

            @Override
            public <T> T onSvgElement(@Nullable String id,
                                      @NonNull T element,
                                      @Nullable RectF elementBounds,
                                      @NonNull Canvas canvas,
                                      @Nullable RectF canvasBounds,
                                      @Nullable Paint paint) {
                if (changeColor && paint != null && paint.getStyle() == Paint.Style.FILL &&
                        ("shirt".equals(id) || "hat".equals(id) || "pants".equals(id))) {
                    Random random = new Random();
                    paint.setColor(Color.argb(255, random.nextInt(256),
                            random.nextInt(256), random.nextInt(256)));
                }
                return element;
            }

            @Override
            public <T> void onSvgElementDrawn(@Nullable String id,
                                              @NonNull T element,
                                              @NonNull Canvas canvas,
                                              @Nullable Paint paint) {
            }

        });
        mSvg.getSharpPicture(new Sharp.PictureCallback() {
            @Override
            public void onPictureReady(SharpPicture picture) {
                Drawable drawable = picture.getDrawable();
                if (mRenderBitmap) {
                    // Create a bitmap with a size that is somewhat arbitrarily determined by SharpDrawable
                    // This will no doubt look bad when scaled up, so perhaps a different dimension would be used in practice
                    int width = Math.max(1, drawable.getIntrinsicWidth());
                    int height = Math.max(1, drawable.getIntrinsicHeight());
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    // Draw SharpDrawable onto this bitmap
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);

                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

                    // You could do some bitmap operations here that aren't supported by Picture
                    //bitmapDrawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                    //bitmapDrawable.setAlpha(100);

                    // Use the BitmapDrawable instead of the SharpDrawable
                    drawable = bitmapDrawable;
                } else {
                    SharpDrawable.prepareView(mImageView);
                }
                mImageView.setImageDrawable(drawable);

                // We don't want to use the same drawable, as we're specifying a custom size; therefore
                // we call createDrawable() instead of getDrawable()
                int iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);
                mButton.setCompoundDrawables(
                        picture.createDrawable(mButton, iconSize),
                        null, null, null);
            }
        });
    }

}
