(ns gcontacts2mutt.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io])
  (:require [clojure.data.csv :as csv]))

(def email-columns [{:type 29 :address 30}
                    {:type 31 :address 32}
                    {:type 33 :address 34}])

(defn create-alias
  [name, type]
  (let [mod-type (if (str/starts-with? type "*") "" (str "_" type))]
    (str (str/replace (str/lower-case name) " " "_") mod-type))
  )

(defn adr-transducer-maker
  [email-column]
  (let [address-col (:address email-column)
        type-col (:type email-column)]
    (comp
     (filter #(not (empty? (get % address-col))))
     (map #(str "alias " (create-alias (get % 0) (get % type-col))
                " " (get % 0) " <" (get % address-col) ">\n")))))

(defn convert-csv2str
  [csv email-column]
  (transduce (adr-transducer-maker email-column) str csv))

(defn read-addressbook-csv
  [filename]
  (with-open [rdr (io/reader filename)]
    (let [csv (csv/read-csv rdr)]
      (reduce str (map #(convert-csv2str csv %) email-columns)))))

(defn -main
  "I don't do a whole lot ... yet."
  [infilename outfilename & args]
  (spit outfilename (read-addressbook-csv infilename))
  (println "Email aliases erzeugt")
  0)
