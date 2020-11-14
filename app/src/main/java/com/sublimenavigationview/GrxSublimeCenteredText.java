package com.sublimenavigationview;


/*
 * Grouxho - espdroids.com - 2018

 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.

 */

public class GrxSublimeCenteredText extends SublimeBaseMenuItem {

    private static final String TAG = GrxSublimeCenteredText.class.getSimpleName();

    public GrxSublimeCenteredText(SublimeMenu menu, int group, int id,
                                  CharSequence title, CharSequence hint,
                                  boolean valueProvidedAsync) {
        super(menu, group, id, title, hint, ItemType.CENTERED, valueProvidedAsync, false);


    }

    public GrxSublimeCenteredText(int group, int id,
                                  CharSequence title, CharSequence hint,
                                  int iconResId,
                                  boolean valueProvidedAsync,
                                  int flags) {
        super(group, id, title, hint, iconResId, ItemType.CENTERED,
                valueProvidedAsync, false, flags);
    }

    @Override
    public boolean invoke() {
        return invoke(OnNavigationMenuEventListener.Event.CLICKED, this);
    }
}
