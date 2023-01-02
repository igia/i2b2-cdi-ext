/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2021-2022 Persistent Systems, Inc.
 */
package io.igia.i2b2.cdi.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationErrorMessageUtil {

    private ValidationErrorMessageUtil (){}
    
    /**
     * Get trimmed validation error messages.
     * @return
     */
    public static String trim(String errMessage) {
        
        Pattern pattern = Pattern.compile("default message \\[(.*?)\\]");
        Matcher matcher = pattern.matcher(errMessage);
        int count = 1;
        List<String> errMessages = new ArrayList<>();
        while(matcher.find()) {
            if (count % 2 == 0) {
                errMessages.add(matcher.group(1));
            }
            count ++ ;
        }
        return String.join(", ", errMessages);
    }
}