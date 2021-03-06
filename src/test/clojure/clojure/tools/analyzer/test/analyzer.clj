(ns clojure.tools.analyzer.test.analyzer
  (:refer-clojure :exclude [macroexpand])
  (:require [clojure.test :refer :all]
            [clojure.tools.analyzer :refer :all]
            [clojure.tools.analyzer.emit-form :refer [emit-form]]))

(deftest test-ast
  (is (= (-> (ast 1) :op)
         :number)))

(deftest test-ast-in-ns
  (is (= (-> (ast-in-ns clojure.core 1) :op)
         :number)))

(deftest test-keyword-invoke-kw
  (is (= (-> (ast (let [a {}] (:abc a)))
           :fexpr :methods first :body :exprs first :body :exprs first :kw :val)
         :abc)))

(deftest test-refer
  ; changed Var mappings do not apply over members of the same `do`
  (is (= (-> (ast (do (require '[clojure.set])
                    (refer 'clojure.set 
                           :only '[intersection] 
                           :rename '{intersection require})
                    require))
           :exprs last :var)
         #'clojure.core/require)) ;should be `clojure.set/intersection`
  (is (analyze-ns 'clojure.tools.analyzer.test.require)))

(deftest records-test
  (is (analyze-ns 'clojure.tools.analyzer.test.records)))

(deftest source-name-test
  (is (-> (analyze-ns 'clojure.tools.analyzer.test.records)
          first :env :source-path str)))

(deftest meta-op-test
  (is (= (-> (with-meta {} {:a 1})
             analyze-form 
             emit-form
             meta)
         {:a 1})))
