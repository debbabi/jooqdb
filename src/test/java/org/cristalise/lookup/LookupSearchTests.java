/**
 * This file is part of the CRISTAL-iSE jOOQ Cluster Storage Module.
 * Copyright (c) 2001-2017 The CRISTAL Consortium. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package org.cristalise.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.cristalise.kernel.lookup.AgentPath;
import org.cristalise.kernel.lookup.DomainPath;
import org.cristalise.kernel.lookup.ItemPath;
import org.cristalise.kernel.lookup.Path;
import org.cristalise.kernel.lookup.RolePath;
import org.junit.Before;
import org.junit.Test;

public class LookupSearchTests extends LookupTestBase {

    UUID uuid0 = new UUID(0,0);
    UUID uuid1 = new UUID(0,1);
    UUID uuid2 = new UUID(0,2);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        lookup.add( new ItemPath(uuid0) );
        lookup.add( new AgentPath(uuid1, "Jim") );
        lookup.add( new AgentPath(uuid2, "John") );
        lookup.add( new DomainPath("empty/nothing") );
        lookup.add( new DomainPath("empty/something/uuid0", lookup.getItemPath(uuid0.toString())) );
//        lookup.add( new DomainPath("empty.old/something/uuid1", lookup.getItemPath(uuid1.toString())) );
        lookup.add( new RolePath(new RolePath(), "User") );
        lookup.add( new RolePath(new RolePath(), "User/SubUser") );
        lookup.add( new RolePath(new RolePath(), "User/SubUser/DummyUser") );
        lookup.add( new RolePath(new RolePath(), "User/LowerUser") );
    }

    @Test
    public void search() throws Exception {
        List<Path> expected = Arrays.asList(new DomainPath("empty"), 
                                            new DomainPath("empty/nothing"), 
                                            new DomainPath("empty/something"), 
                                            new DomainPath("empty/something/uuid0", new ItemPath(uuid0)));

        compare(expected, lookup.search(new DomainPath("empty"), ""));

        expected = Arrays.asList(new DomainPath("empty/something/uuid0", new ItemPath(uuid0)));

        compare(expected, lookup.search(new DomainPath("empty"), "uuid0"));
    }

    @Test
    public void searchAliases() throws Exception {
        ItemPath ip = lookup.getItemPath(uuid0.toString());
        lookup.add( new DomainPath("empty/something/uuid0prime", ip) );

        compare(Arrays.asList(new DomainPath("empty/something/uuid0prime", ip),  new DomainPath("empty/something/uuid0", ip)), 
                lookup.searchAliases(new ItemPath(uuid0)));
    }

    @Test
    public void getChildren_DomainPath() throws Exception {
        lookup.add( new DomainPath("dummy") );

        compare(Arrays.asList(new DomainPath("empty"), new DomainPath("dummy")), lookup.getChildren(new DomainPath()) );

        compare(Arrays.asList(new DomainPath("empty/nothing"),  new DomainPath("empty/something")), 
            lookup.getChildren(new DomainPath("empty")));
    }

    @Test
    public void getChildren_RolePath() throws Exception {
        compare(Arrays.asList(new RolePath(new RolePath("User", false), "SubUser"),  new RolePath(new RolePath("User", false), "LowerUser")),
            lookup.getChildren(new RolePath(new RolePath(), "User")));
    }

    @Test
    public void getChildren_WithDots() throws Exception {
        lookup.add( new DomainPath("empty/nothing.old") );
        lookup.add( new DomainPath("empty/nothing.new/toto") );

        compare(Arrays.asList(new DomainPath("empty/nothing"),
                              new DomainPath("empty/something"),
                              new DomainPath("empty/nothing.old"), 
                              new DomainPath("empty/nothing.new")), 
                lookup.getChildren(new DomainPath("empty")));
    }

    @Test
    public void resolvePath() throws Exception {
        ItemPath ip = lookup.resolvePath(new DomainPath("empty/something/uuid0", new ItemPath(uuid0)));
        assertNotNull(ip);
        assertEquals(uuid0, ip.getUUID());
    }

    @Test
    public void getAgentName() throws Exception {
        assertEquals("Jim",  lookup.getAgentName(new AgentPath(uuid1)));
        assertEquals("John", lookup.getAgentName(new AgentPath(uuid2)));
    }
}
