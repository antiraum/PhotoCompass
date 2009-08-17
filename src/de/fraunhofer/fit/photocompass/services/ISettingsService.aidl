package de.fraunhofer.fit.photocompass.services;

import de.fraunhofer.fit.photocompass.model.Settings;

/**
 * Interface for registering to the settings service
 */
interface ISettingsService {

    Settings getSettings();
    
    void updateSettings(in Settings settings);
}