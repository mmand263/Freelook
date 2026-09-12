package org.freelook.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.freelook.gui.FreeLookConfigScreen;

public class FreeLookModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FreeLookConfigScreen::new;
    }
}
