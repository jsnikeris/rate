(ns rate.db
  (:require [clojure.java.jdbc :as j]))

(def old-db-spec
  {:dbtype "postgres"
   :dbname "old"
   :user "old"
   :password "hehehe"
   :port 5432})

(def new-db-spec
  {:dbtype "postgres"
   :dbname "new"
   :user "new"
   :password "hahaha"
   :port 5433})

(defn read-accounts [db-spec]
  (j/query db-spec "SELECT id, name, email FROM accounts"))

(defn read-old-accounts []
  (read-accounts old-db-spec))

(defn read-new-accounts []
  (read-accounts new-db-spec))
