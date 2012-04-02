(ns clojurewerkz.elastisch.rest
  (:refer-clojure :exclude [get])
  (:require [clojurewerkz.elastisch.utils   :as utils]
            [clj-http.client   :as http]
            [clojure.data.json :as json]))

(defrecord ElasticSearchEndpoint
    [uri version])

(def ^:const throw-exceptions false)

(def ^{:dynamic true} *endpoint* (ElasticSearchEndpoint. "http://localhost:9200" ""))

;; FIXME: rewrite that to macros

(defn post
  [^String uri &{ :keys [body] :as options }]
  (io! (json/read-json
   (:body (http/post uri (merge options { :accept :json :body (json/json-str body) }))))))

(defn put
  [^String uri &{ :keys [body] :as options}]
  (io! (json/read-json
   (:body (http/put uri (merge options { :accept :json :body (json/json-str body)  :throw-exceptions throw-exceptions }))))))

(defn get
  [^String uri]
  (io! (json/read-json
   (:body (http/get uri { :accept :json :throw-exceptions throw-exceptions })))))

(defn head
  [^String uri]
  (io! (http/head uri { :accept :json :throw-exceptions throw-exceptions })))

(defn delete
  [^String uri]
  (io! (json/read-json
   (:body (http/delete uri { :accept :json :throw-exceptions throw-exceptions })))))


(defn base
  []
  (:uri *endpoint*))

(defn index
  [^String index-name]
  (format "%s/%s" (base) index-name))

(defn index-type
  [^String index-name ^String index-type params]
  (if (empty? (keys params))
    (format "%s/%s/%s" (base) index-name index-type)
    (format "%s/%s/%s" (base) index-name index-type (utils/join-hash params))))

(defn search
  [^String index-name ^String index-type params]
  (if (empty? (keys params))
    (format "%s/%s/%s/_search" (base) index-name index-type)
    (format "%s/%s/%s/_search" (base) index-name index-type (utils/join-hash params))))

(defn record
  [^String index-name ^String type id params]
  (if (empty? (keys params))
    (format "%s/%s/%s/%s" (base) index-name type id)
    (format "%s/%s/%s/%s?%s" (base) index-name type id (utils/join-hash params))))


(defn- _index-mapping
  "Returns index mapping"
  ([^String index-name]
     (format "%s/%s/_mapping" (base) index-name))
  ([^String index-name ^String index-type]
     (format "%s/%s/%s/_mapping" (base) index-name index-type)))

(defn index-mapping
  "Returns index mapping"
  ([^String index-name & [ ^String index-type ^Boolean ignore-conflicts ]]
     (let [url (if (nil? index-type) (_index-mapping index-name) (_index-mapping index-name index-type))]
       (if (nil? ignore-conflicts)
         url
         (format "%s?ignore_conflicts=%s" url (.toString ignore-conflicts))))))

(defn index-settings
  ([]
     (format "%s/_settings" (base)))
  ([^String index-name]
    (format "%s/%s/_settings" (base) index-name)))

(defn index-open
  [^String index-name]
  (format "%s/%s/_open" (base) index-name))

(defn index-close
  [^String index-name]
  (format "%s/%s/_close" (base) index-name))

(defn index-mget
  ([]
    (format "%s/_mget" (base)))
  ([^String index-name]
    (format "%s/%s/_mget" (base) index-name))
  ([^String index-name ^String index-type]
    (format "%s/%s/%s/_mget" (base) index-name index-type)))

(defn index-refresh
  ([]
    (format "%s/_refresh" (base)))
  ([^String index-name]
    (format "%s/%s/_refresh" (base) index-name))
  ([^String index-name ^String index-type]
    (format "%s/%s/%s/_refresh" (base) index-name index-type)))


(defn connect
  [uri]
  (let [response (get uri)]
    (ElasticSearchEndpoint. uri
                            (:number (:version response)))))

(defn connect!
  [uri]
  (alter-var-root (var *endpoint*) (constantly (connect uri))))