(ns metabase.query-processor-test.share-test
  (:require [metabase.models
             [metric :refer [Metric]]
             [segment :refer [Segment]]]
            [metabase.query-processor-test :refer :all]
            [metabase.test.data :as data]
            [metabase.test.data.datasets :as datasets]
            [toucan.util.test :as tt]))

(datasets/expect-with-drivers (non-timeseries-drivers-with-feature :basic-aggregations)
  0.94
  (->> {:aggregation [[:share [:< [:field-id (data/id :venues :price)] 4]]]}
       (data/run-mbql-query venues)
       rows
       ffirst
       double))

(datasets/expect-with-drivers (non-timeseries-drivers-with-feature :basic-aggregations)
  nil
  (->> {:aggregation [[:share [:< [:field-id (data/id :venues :price)] 4]]]
        :filter      [:> [:field-id (data/id :venues :price)] Long/MAX_VALUE]}
       (data/run-mbql-query venues)
       rows
       ffirst))

(datasets/expect-with-drivers (non-timeseries-drivers-with-feature :basic-aggregations)
  [[2 0M]
   [3 0M]
   [4 0.5M]
   [5 0.14285714285714285714285714M]]
  (->> {:aggregation [[:share [:< [:field-id (data/id :venues :price)] 2]]]
        :breakout    [[:field-id (data/id :venues :category_id)]]
        :limit       4}
       (data/run-mbql-query venues)
       rows))

(datasets/expect-with-drivers (non-timeseries-drivers-with-feature :basic-aggregations)
  1.47
  (->> {:aggregation [[:+ [:/ [:share [:< [:field-id (data/id :venues :price)] 4]] 2] 1]]}
       (data/run-mbql-query venues)
       rows
       ffirst
       double))

(datasets/expect-with-drivers (non-timeseries-drivers-with-feature :basic-aggregations)
  0.94
  (tt/with-temp* [Segment [{segment-id :id} {:table_id   (data/id :venues)
                                             :definition {:source-table (data/id :venues)
                                                          :filter       [:< [:field-id (data/id :venues :price)] 4]}}]]
    (->> {:aggregation [[:share [:segment segment-id]]]}
         (data/run-mbql-query venues)
         rows
         ffirst
         double)))

(datasets/expect-with-drivers (non-timeseries-drivers-with-feature :basic-aggregations)
  0.94
  (tt/with-temp* [Metric [{metric-id :id} {:table_id   (data/id :venues)
                                           :definition {:source-table (data/id :venues)
                                                        :aggregation  [:share [:< [:field-id (data/id :venues :price)] 4]]}}]]
    (->> {:aggregation [[:metric metric-id]]}
         (data/run-mbql-query venues)
         rows
         ffirst
         double)))
