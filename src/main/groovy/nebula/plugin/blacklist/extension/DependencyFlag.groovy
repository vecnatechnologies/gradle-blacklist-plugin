/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.blacklist.extension

import nebula.plugin.blacklist.data.DependencyCoordinates
import nebula.plugin.blacklist.data.DependencyCoordinatesCreator
import nebula.plugin.blacklist.data.DependencyCoordinatesCreatorImpl
import org.gradle.api.Project

class DependencyFlag {
    // each entry in 'blocked' is a banned dependency, which can be mapped to a property name used to unban it
    private final Map<DependencyCoordinates, String> blocked = new HashMap<DependencyCoordinates, String>()
    // each entry in 'warned' is a potentially bad dependency, which can be mapped to a property name used to remove the warning
    private final Map<DependencyCoordinates, String> warned = new HashMap<DependencyCoordinates, String>()
    private final DependencyCoordinatesCreator dependencyCoordinatesCreator = new DependencyCoordinatesCreatorImpl()

    void block(String coordinates) {
        blocked << [(dependencyCoordinatesCreator.create(coordinates)):null]
    }

    void block(Map<String, String> coordinates) {
        blocked << [(dependencyCoordinatesCreator.create(coordinates)):null]
    }

    // adds a dependency to 'blocked' with an allowProperty which unbans the dependency if true
    void blockUnless(String coordinates, String allowProperty) {
        blocked << [(dependencyCoordinatesCreator.create(coordinates)):allowProperty]
    }

    void blockUnless(Map<String, String> coordinates, String allowProperty) {
        blocked << [(dependencyCoordinatesCreator.create(coordinates)):allowProperty]
    }

    boolean containsBlocked(DependencyCoordinates target, Project project) {
        for (e in blocked) {
            DependencyCoordinates blockedDependency = e.key
            String allowProperty = e.value
            if (allowProperty == null || !project.ext.has(allowProperty) || !project.ext[allowProperty]) {
                if (target.equals(blockedDependency)) {
                    return true
                }
            }
        }
        return false
    }

    void warn(String coordinates) {
        warned << [(dependencyCoordinatesCreator.create(coordinates)):null]
    }

    void warn(Map<String, String> coordinates) {
        warned << [(dependencyCoordinatesCreator.create(coordinates)):null]
    }

    // adds a dependency to 'warned' with an allowProperty which removes the warning if true
    void warnUnless(String coordinates, String allowProperty) {
        warned << [(dependencyCoordinatesCreator.create(coordinates)):allowProperty]
    }

    void warnUnless(Map<String, String> coordinates, String allowProperty) {
        warned << [(dependencyCoordinatesCreator.create(coordinates)):allowProperty]
    }

    boolean containsWarned(DependencyCoordinates target, Project project) {
        for (e in warned) {
            DependencyCoordinates warnDependency = e.key
            String allowProperty = e.value
            if (allowProperty == null || !project.ext.has(allowProperty) || !project.ext[allowProperty]) {
                if (target.equals(warnDependency)) {
                    return true
                }
            }
        }
        return false
    }

    boolean hasMappings() {
        !blocked.isEmpty() || !warned.isEmpty()
    }
}
