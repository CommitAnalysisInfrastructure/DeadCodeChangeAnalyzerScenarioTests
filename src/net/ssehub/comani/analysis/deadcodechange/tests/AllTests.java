/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.ssehub.comani.analysis.deadcodechange.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.comani.analysis.deadcodechange.tests.artificial.ArtificialCommitsAllBlocksTests;
import net.ssehub.comani.analysis.deadcodechange.tests.artificial.ArtificialCommitsConfigBlocksTests;
import net.ssehub.comani.analysis.deadcodechange.tests.linux.LinuxCommitsAllBlocksTests;
import net.ssehub.comani.analysis.deadcodechange.tests.linux.LinuxCommitsConfigBlocksTests;

/**
 * Definition of this test suite.
 */
@RunWith(Suite.class)
@SuiteClasses({
    ArtificialCommitsAllBlocksTests.class,
    ArtificialCommitsConfigBlocksTests.class,
    LinuxCommitsAllBlocksTests.class,
    LinuxCommitsConfigBlocksTests.class
    })

/**
 * This class summarizes all individual test classes into a single test suite.
 * 
 * @author Christian Kroeher
 *
 */
public class AllTests {
    
}
