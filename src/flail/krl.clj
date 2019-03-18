(ns flail.krl)

(declare emit-program)

(def globals
  #{"COM_ROUNDM",
    "COM_ACTION",
    "COM_ACTCNT",
    "COM_E6AXIS",
    "COM_FRAME",
    "COM_POS",
    "COM_VALUE1",
    "COM_VALUE2",
    "COM_VALUE3",
    "COM_VALUE4"})

(defrecord IntegerVar [name])

(defn tab-right [text]
  (let [lines (clojure.string/split text #"\n")
        tabbed-lines (map #(str "  " %) lines)]
    (str (clojure.string/join "\n" tabbed-lines) "\n")))

(defn emit-line [& pieces]
  (str (clojure.string/join pieces) "\n"))

(defn emit-comment [text]
  (emit-line "; " text))

(defn emit-frame [{ x :x y :y z :z a :a b :b c :c }]
  (str "{FRAME: " "X " x ",Y " y ",Z " z ",A " a ",B " b ",C " c "}"))

(def current-pose "$AXIS_ACT")

(defn ptp [pose]
  (emit-line "PTP " pose))

(defn ptp-approximate [pose]
  (emit-line "PTP " pose " C_PTP"))

(defn lin [pose]
  (emit-line "LIN " pose))

(defn lin-approximate [pose]
  (emit-line "LIN " pose "C_DIS"))

(defn circle [position frame]
  (emit-line "CIRC " position "," frame))

(defn circle-approximate [position frame]
  (emit-line "CIRC " position "," frame " C_DIS"))

(defn set-orientation-mode [mode]
  (case mode
    :constant (emit-line "$ORI_TYPE = #CONSTANT")
    :variable (emit-line "$ORI_TYPE = #VAR")
    :joint (emit-line "$ORI_TYPE = #JOINT")
    (throw (RuntimeException. "Unknown orientation mode"))))

(defn set-circular-motion-mode [mode]
  (case mode
    :base (emit-line "$CIRC_TYPE = #BASE")
    :path (emit-line "$CIRC_TYPE = #PATH")
    (throw (RuntimeException. "Unknown circular motion mode"))))

(defn set-swivel-velocity [velocity]
  (emit-line "$VEL.ORI1 = " velocity))

(defn set-rotational-velocity [velocity]
  (emit-line "$VEL.ORI2 = " velocity))

(defn set-swivel-acceleration [acceleration]
  (emit-line "$ACC.ORI1 = " acceleration))

(defn set-rotational-acceleration [acceleration]
  (emit-line "$ACC.ORI2 = " acceleration))

(defn set-center-point-velocity [velocity]
  (emit-line "$VEL.CP = " velocity))

(defn set-center-point-acceleration [acceleration]
  (emit-line "$ACC.CP = " acceleration))

(defn set-lookahead [lookahead]
  (emit-line "$ADVANCE = " lookahead))

(defn set-approximation-range [approx-range]
  (emit-line "$APO.CPTP = " approx-range))

(defn set-distance-criterion [criterion]
  (emit-line "$APO.CDIS = " criterion))

(defn set-base-frame [frame]
  (if (= (type frame) java.lang.String)
    (emit-line "$BASE = " frame)
    (emit-line "$BASE = " (emit-frame frame))))

(defn set-tool-frame [frame]
  (if (= (type frame) java.lang.String)
    (emit-line "$TOOL = " frame)
    (emit-line "$TOOL = " (emit-frame frame))))

(defn set-axis-velocity [joint-id velocity]
  (emit-line "$VEL_AXIS[" joint-id "] = " velocity))

(defn set-axis-acceleration [joint-id acceleration]
  (emit-line "$ACC_AXIS[" joint-id "] = " acceleration))

(defn wait [seconds]
  (emit-line "WAIT SEC " (str seconds)))

(defn sync-lookahead [] (wait 0))

; TODO convenience function for setting motion modes and limits all at once in
; one call to something like motion-settings

; Challenges
; * Find local variables and declare them uniquely above BAS(#INITMOV,0)

; if it's a regular function then just run it and accumulate its string value
; if it's defn, def, while, case, or if then accumulate the corresponding code
; otherwise, error out
(defn emit-function [[fn-name args body]]
  (str
    (emit-line "DEF " (name fn-name) "(" (clojure.string/join " " args) ")")
    (tab-right (emit-program body))
    (emit-line "END")))

; FIXME make smarter so it can rewrite nested/compound conditions
; FIXME add failing tests!
(defn emit-condition [[op val1 val2]]
  (str val1 " " op " " val2))

(defn replace-vars [form]
  ;(if (= (type form) clojure.lang.Symbol)
  (if (= (type form) clojure.lang.PersistentList)
    (map replace-vars form)
    (if (contains? globals (str form)) (str form) form)))

(defn emit-assignment [var-name value]
  (emit-line var-name " = " value))

(defn emit-case [[value form]]
  (str
    (emit-line "CASE " value)
    (tab-right (emit-program form))))

(defn emit-switch [[value & cases]]
  (str
    (emit-line "SWITCH " value)
    (clojure.string/join (map #(tab-right (emit-case %)) (partition 2 cases)))
    (emit-line "ENDSWITCH")))

(defn emit-if [[condition body-a body-b]]
  (str
    (emit-line "IF " (emit-condition condition) " THEN")
    (tab-right (emit-program body-a))
    (if (not (nil? body-b))
      (str
        (emit-line "ELSE")
        (tab-right (emit-program body-b))))
    (emit-line "ENDIF")))

(defn emit-while [[condition body]]
  (str
    (emit-line "WHILE " (emit-condition condition))
    (tab-right (emit-program body))
    (emit-line "ENDWHILE")))

(defmacro for-loop [variable start end body]
  `(str
    (emit-line "FOR " ~variable "=" ~start " TO " ~end)
    (tab-right (emit-program '~body))
    (emit-line "ENDFOR")))

(defn emit-program [program-form]
  (case (name (first program-form))
    ; TODO handle arguments <- make a failing test!
    "defn" (emit-function (rest program-form))
    "def" (let [[var-name value] (rest program-form)]
            (emit-assignment var-name value))
    "while" (emit-while (rest program-form))
    "case" (emit-switch (rest program-form))
    "if" (emit-if (rest program-form))
    "do" (apply str (map emit-program (rest program-form)))
    ; It's unknown, so just try evaluating it and see if you get a string
    (let [result (eval (replace-vars program-form))]
      (if (= (type result) java.lang.String)
        result
        (throw (RuntimeException. (str "Unknown form: " program-form)))))))

(defn emit-init []
  (str
    (emit-line "&ACCESS RVP")
    (emit-line "&REL 1")))

(defmacro script [name program-form]
  `(str
     (emit-comment ~name)
     (emit-init)
     (emit-program '~program-form)))

