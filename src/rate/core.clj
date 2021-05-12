(ns rate.core
  (:require [rate.db :as db]
            [clojure.set :as s])
  (:gen-class))

(defn source-accounts [source accounts]
  "Add a source key to each account map"
  (map #(assoc % :source source)))

(defn corrupt?
  "Assumes the shared columns are email and name"
  [account-1 account-2]
  (or (not (= (:name account-1) (:name account-2)))
      (not (= (:email account-1) (:email account-2)))))

(defn determine-status
  "Do the sourced accounts represent added, missing, or corrupt data"
  [one-or-two-accounts]
  (if (= (count one-or-two-accounts) 1) ;row found in only one database
    (case (:source (first one-or-two-accounts))
      :old :missing
      :new :added)
    (if (apply corrupt? one-or-two-accounts)
      :corrupt
      :ok)))

(defn build-report-map [old-accounts new-accounts]
  (let [by-id-index (s/index (concat (source-accounts :old old-accounts)
                                     (source-accounts :new new-accounts))
                             [:id])]
    (reduce
     (fn [acc one-or-two-accounts]
       (let [status (determine-status one-or-two-accounts)]
         (if (= status :ok)
           acc
           (update acc status conj one-or-two-accounts))))
     {}
     (vals by-id-index))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
