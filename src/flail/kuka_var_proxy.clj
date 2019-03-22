(ns flail.kuka-var-proxy
  (:require [aleph.tcp :as tcp]
            [gloss.core :refer :all]
            [gloss.io :as io]))

; A description of the protocol used by KukaVarproxy can be found on GitHub:
; https://github.com/ImtsSrl/KUKAVARPROXY/

(defcodec request-frame
  (header
    (ordered-map
      :id :int16
      :content-length :uint16
      :mode (enum :ubyte { :read 0 :write 1 }))
    #({ :read (ordered-map :name (finite-frame :uint16 (string :utf-8)))
        :write (ordered-map
                 :name (finite-frame :uint16 (string :utf-8))
                 :value (finite-frame :uint16 (string :utf-8))) }
      (:mode %))
    ; FIXME implement body->header function
    identity))

(defn decode-request [data]
  "Decode bytes of request data to a map of properties."
  (io/decode request-frame data))

(defcodec response-frame
  (ordered-map
    :id :int16
    :content-length :uint16
    :mode (enum :ubyte { :read 0 :write 1 :read-array 2 :write-array 3 })
    :value (finite-frame :uint16 (string :utf-8))
    :status [:ubyte :ubyte :ubyte])) ; 000 on error, 011 on success

(defn decode-response [data]
  "Decode bytes of response data to a map of properties."
  (io/decode response-frame data))

(defn decode-exchange [data]
  (io/decode-all (compile-frame [request-frame response-frame]) data))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream x) out)
    (.toByteArray out)))

(defn decode-stream-dump-file [filename]
  "Describe the contents of a stream of KukaVarproxy frames (e.g. from Wireshark)."
  (let [frames (decode-exchange (slurp-bytes filename))]
    (print (clojure.string/join "\n" (map str frames)))))

