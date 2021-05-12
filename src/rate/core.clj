(ns rate.core
  (:require [rate.db :as db]
            [clojure.set :as s]
            [clojure.pprint :as pprint])
  (:gen-class))

(defn source-accounts [source accounts]
  "Add a source key to each account map"
  (map #(assoc % :source source) accounts))

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
  ;; values of this index are sets of one or two accounts
  ;; one entry in the set means that account was added or removed
  (let [by-id-index (s/index (concat (source-accounts :old old-accounts)
                                     (source-accounts :new new-accounts))
                             [:id])]
    (reduce
     (fn [acc one-or-two-accounts]
       (let [status (determine-status one-or-two-accounts)
             report-entry (if (= 1 (count one-or-two-accounts))
                            ;; clean up added/missing entries
                            (-> one-or-two-accounts first (dissoc :source))
                            ;; include old and new versions when corrupt
                            one-or-two-accounts)]
         (if (= status :ok)
           acc
           (update acc status conj report-entry))))
     {}
     (vals by-id-index))))

(defn -main
  "Writes a report to standard out"
  [& args]
  (pprint/pprint (build-report-map (db/read-old-accounts)
                                   (db/read-new-accounts))))


