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

import android.util.Log;

public class LQLog {

	public static int LOG_LEVEL = 1;

	private static final int PATHS = 7;
	private static final int HTTP = 6;
	private static final int DATA = 5;
	private static final int INFO_VERBOSE = 4;
	private static final int WARNING = 3;
	private static final int INFO = 2;
	private static final int ERROR = 1;

	public static void paths(String message) {
		if(LOG_LEVEL >= PATHS) {
			Log.d(Liquid.TAG_LIQUID, message);
		}
	}

	public static void http(String message) {
		if(LOG_LEVEL >= HTTP) {
			Log.d(Liquid.TAG_LIQUID, message);
		}
	}

	public static void data(String message) {
		if(LOG_LEVEL >= DATA) {
			Log.v(Liquid.TAG_LIQUID, message);
		}
	}

	public static void infoVerbose(String message) {
		if(LOG_LEVEL >= INFO_VERBOSE) {
			Log.i(Liquid.TAG_LIQUID, message);
		}
	}

	public static void warning(String message) {
		if(LOG_LEVEL >= WARNING) {
			Log.w(Liquid.TAG_LIQUID, message);
		}
	}

	public static void info(String message) {
		if(LOG_LEVEL >= INFO) {
			Log.i(Liquid.TAG_LIQUID, message);
		}
	}

	public static void error(String message) {
		if(LOG_LEVEL >= ERROR) {
			Log.e(Liquid.TAG_LIQUID, message);
		}
	}

}
