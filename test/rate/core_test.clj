(ns rate.core-test
  (:require [clojure.test :refer :all]
            [rate.core :refer :all]))

(def jim-account {:id 123 :name "Jim" :email "jim@a.com"})
(def bob-account {:id 456 :name "Bob" :email "bob@a.com"})

(deftest source-accounts-test
  (is (= :old
       (-> (source-accounts :old [jim-account bob-account])
           first
           :source))))

(deftest corrupt?-test
  (is (corrupt? jim-account
                (assoc jim-account :name "Bob")))
  (is (corrupt? jim-account
                (assoc jim-account :email "bob@a.com")))
  (is (not (corrupt? jim-account jim-account)))
  (is (not (corrupt? jim-account (assoc jim-account :color "green")))))

(deftest determine-status-test
  (is (= :added (determine-status [(assoc jim-account :source :new)])))
  (is (= :missing (determine-status [(assoc jim-account :source :old)])))
  (is (= :ok (determine-status [jim-account jim-account])))
  (is (= :corrupt (determine-status [jim-account bob-account]))))

(deftest build-report-map-test
  (let [report-map (build-report-map
                    ;; old accounts
                    [jim-account bob-account]
                    ;; new accounts
                    [(assoc jim-account :name "Steve")
                     {:id 789 :name "Tom"}])]
    (testing "added"
      (is (= 1 (-> report-map :added count)))
      (is (= 789 (-> report-map :added first :id))))
    (testing "missing"
      (is (= 1 (-> report-map :missing count)))
      (is (= (:id bob-account) (-> report-map :missing first :id))))
    (testing "corrupt"
      (is (= 1 (-> report-map :corrupt count)))
      (is (= (:id jim-account) (-> report-map :corrupt first first :id))))))
