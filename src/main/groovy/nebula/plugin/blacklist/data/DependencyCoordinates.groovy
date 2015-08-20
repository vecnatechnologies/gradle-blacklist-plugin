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
package nebula.plugin.blacklist.data

class DependencyCoordinates {
    final String group
    final String name
    final String version

    DependencyCoordinates(String group, String name = null, String version = null) {
        assert group != null, 'Group attribute may not be null'
        this.group = group
        this.name = name
        this.version = version
    }

    /*
        Checks to see whether this.version is contained by otherVersion
        This is useful for blocking dependencies that match
          a flagged dependency with a less specific version range
        Dependency ranges look like {comparison_operator}version
          where {comparison_operator} is one of {'>', '<', '>=', '<='}
        For example, <=4.10 contains any version less than or equal to 4.10 
    */
    private boolean containedByVersion(String otherVersion) {
        if (otherVersion == null) { // specific version should match no version specified
            return true
        }
        String comparator
        if (otherVersion.startsWith('<=')) {
            comparator = '<='
            otherVersion = otherVersion.substring(2)
        } else if (otherVersion.startsWith('>=')) {
            comparator = '>='
            otherVersion = otherVersion.substring(2)
        } else if (otherVersion.startsWith('<')) {
            comparator = '<'
            otherVersion = otherVersion.substring(1)
        } else if (otherVersion.startsWith('>')) {
            comparator = '>'
            otherVersion = otherVersion.substring(1)
        } else {
            comparator = '='
        }
        return versionCompare(version, otherVersion, comparator)
    }

    /*
        Compares two versions, str1 and str2, and checks whether comparator is true
    */
    private static versionCompare(String str1, String str2, String comparator) {
        Scanner s1 = new Scanner(str1)
        Scanner s2 = new Scanner(str2)
        s1.useDelimiter("\\.")
        s2.useDelimiter("\\.")
        String actualComparison = ''

        while(s1.hasNextInt() && s2.hasNextInt()) {
            int v1 = s1.nextInt()
            int v2 = s2.nextInt()
            if(v1 < v2) {
                actualComparison = '<'
                break
            } else if(v1 > v2) {
                actualComparison = '>'
                break
            }
        }
        if (actualComparison.equals('')){
            if (s1.hasNextInt()) actualComparison = '>' // str1 has an additional lower-level version number
            else if (s2.hasNextInt()) actualComparison = '<' // str2 has an additional lower-level version number
            else actualComparison = '='
        }
        return comparator.contains(actualComparison)
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        DependencyCoordinates that = (DependencyCoordinates) o

        if (group != that.group) return false
        if (name != that.name) return false
        if (!containedByVersion(that.version)) return false

        return true
    }

    @Override
    int hashCode() {
        int result
        result = group.hashCode()
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }

    @Override
    String toString() {
        StringBuilder coordinatesString = new StringBuilder()
        coordinatesString <<= group
        coordinatesString <<= ':'

        if(name) {
            coordinatesString <<= name
        }

        coordinatesString <<= ':'

        if(version) {
            coordinatesString <<= version
        }

        coordinatesString.toString()
    }

    static enum Notation {
        GROUP('group'), NAME('name'), VERSION('version')

        private final String attribute

        private Notation(String attribute) {
            this.attribute = attribute
        }

        String getAttribute() {
            return attribute
        }

        static List<String> getAllAttributes() {
            values().collect { it.attribute }
        }
    }
}
