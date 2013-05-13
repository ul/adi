(ns adi.emit.test-adjust
  (:use midje.sweet
        hara.common
        adi.emit.adjust))

(fact "adjust-safe-check"
  (adjust-safe-check (fn [x] (throw (Exception.))) :anything {})
  => falsey

  (adjust-safe-check long? "1" {})
  => falsey

  (adjust-safe-check long? 2 {})
  => true

  (adjust-safe-check string? "1" {})
  => true

  (adjust-safe-check long? '_ {})
  => true

  (adjust-safe-check (fn [x] (throw (Exception.))) '_ {})
  => true

  (adjust-safe-check long? '[< _ 3] {})
  => falsey

  (adjust-safe-check long? '[< _ 3] {:options {:query? true}})
  => true)

(fact "adjust-value-sets-only"
  (adjust-value-sets-only #{} string? {} nil)
  => #{}

  (adjust-value-sets-only "1" string? {} nil)
  => #{"1"}

  (adjust-value-sets-only #{"1"} string? {} nil)
  => #{"1"}

  (adjust-value-sets-only #{"1" "2"} string? {} nil)
  => #{"1" "2"}

  (adjust-value-sets-only 1 string? {} nil)
  => (throws Exception)

  (adjust-value-sets-only #{1} string? {} nil)
  => (throws Exception)

  (adjust-value-sets-only #{1 "2"} string? {} nil)
  => (throws Exception)

  (adjust-value-sets-only #{'[< _ 3] '[> _ 6]} long?
                          {:options {:query? true}} nil)
  => #{'[< _ 3] '[> _ 6]}
  )

(fact "adjust-value-normal"
  (adjust-value-normal "1" {} string? {} nil nil)
  => "1"

  (adjust-value-normal #{"1"} {} string? {} nil nil)
  => (throws Exception)

  (adjust-value-normal #{} {:cardinality :many} string? {} nil nil)
  => #{}

  (adjust-value-normal "1" {:cardinality :many} string? {} nil nil)
  => #{"1"}

  (adjust-value-normal #{"1" "2"} {:cardinality :many} string? {} nil nil)
  => #{"1" "2"}

  (adjust-value-normal "1" {} long? {} nil nil)
  => (throws Exception)

  (adjust-value-normal "1" {:cardinality :many} long? {} nil nil)
  => (throws Exception)

  (adjust-value-normal #{"1"} {:cardinality :many} long? {} nil nil)
  => (throws Exception))

(fact "adjust-value"
  (adjust-value "1" {} string? {} nil nil) => "1"

  (adjust-value "1" {} string?
                {:options {:sets-only? true}} nil nil)
  => #{"1"}

  (adjust-value '[< _ 3] {} string?
                {:options {:sets-only? true
                           :query? true}} nil nil)
  => #{'[< _ 3]})

(fact "adjust-chk-type"
  (adjust-chk-type "1" {:type :string} {}) => "1"

  (adjust-chk-type "1" {:type :long} {}) => (throws Exception)

  (adjust-chk-type "1" {:type :string
                    :cardinality :many} {})
  => #{"1"}

  (adjust-chk-type "1" {:type :string}
               {:options {:sets-only? true}})
  => #{"1"})

(fact "adjust-chk-restrict"
  (adjust-chk-restrict 1 {:restrict odd?}
                   {:options {:restrict? true}})
  => 1

  (adjust-chk-restrict 2 {:restrict odd?}
                   {:options {:restrict? true}})
  => (throws Exception)

  (adjust-chk-restrict 2 {:restrict odd?} {})
  => 2

  (adjust-chk-restrict 2 {} {:options {:restrict? true}})
  => 2)

(fact "adjust use cases"
  (adjust "1" {:type :string} {})
  => "1"

  (adjust "1" {:type :long} {})
  => (throws Exception)

  (adjust 2 {:type :long
             :restrict? even?}
          {:options {:restrict? true}})
  => 2

  (adjust 2 {:type :long
             :restrict? even?}
          {:options {:restrict? true
                     :sets-only? true}})
  => #{2}


  (adjust #{2 4 6 8}
          {:type        :long
           :cardinality :many
           :restrict    even?}
          {:options {:restrict? true}})
  => #{2 4 6 8}

  (adjust 1
          {:type        :long
           :cardinality :many
           :restrict    even?}
          {:options {:restrict? true}})
  => (throws Exception)

  (adjust #{2 4 6 7}
          {:type        :long
           :cardinality :many
           :restrict    even?}
          {:options {:restrict? true}})
  => (throws Exception))
