/**
 * Copyright 2014-present Liquid Data Intelligence S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lqd.sdk;

import java.util.Calendar;
import java.util.Date;

public class UniqueTime {

    private static int mIncrement = 1;

    public static synchronized Date newDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, mIncrement);
        mIncrement = (mIncrement + 1) % 200;
        return cal.getTime();
    }
}
