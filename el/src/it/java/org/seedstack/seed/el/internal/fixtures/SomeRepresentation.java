/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.el.internal.fixtures;

import org.seedstack.seed.it.api.ITBind;

/**
 * @author pierre.thirouin@ext.mpsa.com
 *         Date: 27/06/2014
 */
@ITBind
public class SomeRepresentation {

    private String message = "hello";

    @PreEL("${1+1}")
    @PostEL("${1+1}")
    public void setMessage(String message) {
        this.message = message;
    }

    @PreEL("${zz}")
    public void failingMethod() {
        // do nothing
    }
}