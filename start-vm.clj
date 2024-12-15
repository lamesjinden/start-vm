#!/usr/bin/env bb

(require '[clojure.string :as s])
(require '[babashka.process :as p])

(defn get-running-vms []
  (let [command "vboxmanage list runningvms"]
    (->> (p/shell {:out :string} command)
         :out
         (s/split-lines)
         (map #(s/split % #" "))
         (map first)
         (map #(s/replace % "\"" ""))
         (into #{}))))

(defn run [vm-name start-type]
  (let [running-vm-names (get-running-vms)]
    (when-not (contains? running-vm-names vm-name)
      (let [command (format "vboxmanage startvm %s --type=%s" vm-name start-type)]
        (p/shell command)))))

(defn print-help []
  (println "Description:")
  (println "Starts a named VM. If the VM is already running, no attempt will be made to start it.")
  (println)
  (println "Examples:")
  (println "./start-vm -h ;; prints this message")
  (println "./start-vm server1 [gui|headless|separate] ;; starts the 'server' VM with the specified mode")
  (println "./start-vm server1 ;; starts the 'server1' VM with the default mode (i.e. headless)")
  (println)
  (println "Arguments: <arg1> <arg2>")
  (println "[required] arg1 - the name of the VM to start")
  (println "[optional] arg2 - the mode of the VM to start")
  (println "                  supported values: 'gui', 'headless', 'separate'"))

(defn -main [vm-name & args]
  (if-let [_help (some (fn [x] (or (= x "--help") (= x "-h"))) (cons vm-name args))]
    (print-help)
    (let [start-type (or (first args) "headless")]
      (run vm-name start-type))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))