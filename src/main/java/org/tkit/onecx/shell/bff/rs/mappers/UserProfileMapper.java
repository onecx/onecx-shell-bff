package org.tkit.onecx.shell.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.shell.bff.rs.internal.model.*;
import gen.org.tkit.onecx.user.profile.client.model.UserPerson;
import gen.org.tkit.onecx.user.profile.client.model.UserProfile;
import gen.org.tkit.onecx.user.profile.client.model.UserProfileAccountSettings;

@Mapper
public interface UserProfileMapper {

    @Mapping(target = "userProfile", source = ".")
    GetUserProfileResponseDTO map(UserProfile userProfile);

    UserPersonDTO map(UserPerson person);

    default AccountSettingsDTO map(UserProfileAccountSettings accountSettings) {
        AccountSettingsDTO settingsDTO = new AccountSettingsDTO();
        settingsDTO.setLayoutAndThemeSettings(mapLayoutAndTheme(accountSettings));
        settingsDTO.setLocaleAndTimeSettings(mapLocaleAndTime(accountSettings));
        return settingsDTO;
    }

    LocaleAndTimeSettingsDTO mapLocaleAndTime(UserProfileAccountSettings accountSettings);

    LayoutAndThemeSettingsDTO mapLayoutAndTheme(UserProfileAccountSettings accountSettings);

}
