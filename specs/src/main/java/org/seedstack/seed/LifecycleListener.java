/*
 * Copyright © 2013-2017, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed;

/**
 * Any class implementing this interface will be registered as an application lifecycle listener. As the application
 * starts and stops, these callbacks will be invoked to allow for executing startup and/or shutdown code.
 */
public interface LifecycleListener {

    /**
     * This method is called by Seed just after the application has started up.
     */
    void started();

    /**
     * This method is called by Seed just before the application is shut down.
     */
    void stopping();
}
