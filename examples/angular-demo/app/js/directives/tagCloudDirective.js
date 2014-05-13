/*
 * Copyright 2014 Next Century Corporation
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
 *
 */
'use strict';

/**
 * This directive is for building a tag cloud
 */
angular.module('tagCloudDirective', []).directive('tagCloud', ['ConnectionService', '$timeout',
    function (connectionService, $timeout) {

        return {
            templateUrl: 'partials/tagCloud.html',
            restrict: 'E',
            scope: {
                tagField: '='
            },
            controller: function ($scope) {
            },
            link: function ($scope, element, attr) {
                /**
                 * Initializes the name of the directive's scope variables
                 * and the Neon Messenger used to monitor data change events.
                 * @method initialize
                 */
                $scope.initialize = function () {
                    $scope.databaseName = '';
                    $scope.tableName = '';

                    // data will be a list of tag name/counts in descending order
                    $scope.data = [];

                    $scope.filterKey = neon.widget.getInstanceId("tagcloud");
                    $scope.filterTags = [];
                    $scope.showFilter = false;
                    $scope.$watchCollection('filterTags', $scope.setTagFilter);
                    $scope.andTags = true;

                    // Setup our messenger.
                    $scope.messenger = new neon.eventing.Messenger();

                    $scope.messenger.events({
                        activeDatasetChanged: onDatasetChanged,
                        filtersChanged: onFiltersChanged
                    });

                    // setup tag cloud color/size changes
                    $.fn.tagcloud.defaults = {
                        size: {start: 130, end: 250, unit: '%'},
                        color: {start: '#aaaaaa', end: '#2f9f3e'}
                    };

                };


                /**
                 * Event handler for filter changed events issued over Neon's messaging channels.
                 * @param {Object} message A Neon filter changed message.
                 * @method onFiltersChanged
                 * @private
                 */
                var onFiltersChanged = function (message) {
                    $scope.queryForTags();
                };

                /**
                 * Event handler for dataset changed events issued over Neon's messaging channels.
                 * @param {Object} message A Neon dataset changed message.
                 * @param {String} message.database The database that was selected.
                 * @param {String} message.table The table within the database that was selected.
                 * @method onDatasetChanged
                 * @private
                 */
                var onDatasetChanged = function (message) {
                    $scope.databaseName = message.database;
                    $scope.tableName = message.table;
                    // check if the field was passed in, otherwise check the mapping. if neither is found leave it empty
                    $scope.tagField = $scope.tagField || connectionService.getFieldMapping($scope.databaseName, $scope.tableName, "tags").mapping || '';

                    // if there is no active connection, try to make one.
                    connectionService.connectToDataset(message.datastore, message.hostname, message.database);

                    // Pull data.
                    $scope.clearTagFilters();
                    
                };

                /**
                 * Triggers a query that will aggregate the most popular tags in the tag cloud
                 * @method queryForTags
                 */
                $scope.queryForTags = function () {
                    if ($scope.tagField !== '') {
                        var connection = connectionService.getActiveConnection();
                        if (connection) {
                            var host = connection.host_;
                            var url = neon.serviceUrl('mongotagcloud', 'tagcounts', 'host=' + host + "&db=" + $scope.databaseName + "&collection=" + $scope.tableName + "&arrayfield=" + $scope.tagField + "&limit=30");
                            neon.util.ajaxUtils.doGet(url, {
                                success: function (tagCounts) {
                                    $scope.$apply(function () {
                                        $scope.updateTagData(tagCounts)
                                    });
                                }
                            });
                        }
                    }
                };

                /**
                 * Updates the the tag cloud visualization.
                 * @param {Array} tagCounts An array of objects with "tag" and "count" properties for the tag
                 * name and number of occurrences.
                 * @method updateTagData
                 */
                $scope.updateTagData = function (tagCounts) {

                    if($scope.andTags)
                        $scope.data = tagCounts.filter(function(elem) {
                            return $scope.filterTags.indexOf(elem.tag) == -1;
                        });
                    else
                        $scope.data = tagCounts;

                    // style the tags after they are displayed
                    $timeout(function () {
                        element.find('.tag').tagcloud();
                    });
                };

                /**
                 * Ensures that the tag filter includes the argument, and updates the tag cloud if necessary.
                 * @param tagName {String} the tag that should be filtered on, e.g., "#lol"
                 * @method addTagFilter
                 */
                $scope.addTagFilter = function(tagName) {
                    if ($scope.filterTags.indexOf(tagName) === -1) {
                        $scope.filterTags.push(tagName);
                    }
                };

                /**
                 * Changes the filter to use the ones provided in the first argument.
                 * @param tagNames {Array} an array of tag strings, e.g., ["#lol", "#sad"]
                 * @param oldTagNames {Array} the old array of tag names
                 * @method setTagFilter
                 */
                $scope.setTagFilter = function(tagNames, oldTagNames) {
                    if (tagNames !== oldTagNames) {
                        if (tagNames.length > 0) {
                            var tagFilter = $scope.createFilterForTags(tagNames);
                            $scope.applyFilter(tagFilter);
                        } else {
                            $scope.clearTagFilters();
                        }
                    }
                };

                /**
                 * Creates a filter select object that has a where clause that "or"s all of the tags together
                 * @param tagNames {Array} an array of tag strings that records must have to pass the filter
                 * @returns {Object} a neon select statement
                 * @method createFilterForTags
                 */
                $scope.createFilterForTags = function(tagNames) {
                    var filterClauses = tagNames.map(function(tagName) {
                        return neon.query.where($scope.tagField, "=", tagName);
                    });
                    if($scope.andTags)
                        var filterClause = filterClauses.length > 1 ? neon.query.and.apply(neon.query, filterClauses) : filterClauses[0];
                    else
                        var filterClause = filterClauses.length > 1 ? neon.query.or.apply(neon.query, filterClauses) : filterClauses[0];
                    return new neon.query.Filter().selectFrom($scope.databaseName, $scope.tableName).where(filterClause);
                };

                /**
                 * Applies the specified filter and updates the visualization on success
                 * @param filter {Object} the neon filter to apply
                 * @method applyFilter
                 */
                $scope.applyFilter = function(filter) {
                    $scope.messenger.replaceFilter($scope.filterKey, filter, function () {
                        $scope.$apply(function () {
                            $scope.queryForTags();
                            // Show the Clear Filter button.
                            $scope.showFilter = true;
                            $scope.error = "";
                        });
                    }, function () {
                        // Notify the user of the error.
                        $scope.error = "Error: Failed to apply the filter.";
                    });
                };

                /**
                 * Removes the filter and updates.
                 * @method clearTagFilters
                 */
                $scope.clearTagFilters = function() {
                    $scope.messenger.removeFilter($scope.filterKey, function () {
                        $scope.$apply(function () {
                            $scope.showFilter = false;
                            $scope.error = "";
                            $scope.queryForTags();
                        });
                    }, function () {
                        // Notify the user of the error.
                        $scope.error = "Error: Failed to clear filter.";
                    });
                };

                /**
                 * Remove a particular tag from the filter and update.
                 * @param tagName {String} the tag to remove from the filter, e.g., "#lol"
                 * @method removeFilter
                 */
                $scope.removeFilter = function(tagName) {
                    $scope.filterTags = _.without($scope.filterTags, tagName)
                };

                // Toggle the points and clusters view when the user toggles between them.
                $scope.$watch('andTags', function (newVal, oldVal) {
                    if (newVal !== oldVal) {
                        $scope.setTagFilter($scope.filterTags);
                    }
                });

                // Wait for neon to be ready, the create our messenger and intialize the view and data.
                neon.ready(function () {
                    $scope.initialize();
                });

            }
        }
            ;
    }])
;
