(ns flail.kuka-var-proxy-robot-sync
  (:require [flail.krl :as krl]))

(def sync-script
  (krl/script "KukaVarProxyRobotSync"
    (defn KukaVarProxyRobotSync []
      (do
        (krl/ptp krl/current-pose) ; PTP $AXIS_ACT

        (krl/set-orientation-mode :variable)
        ; $ORI_TYPE  = #VAR
        (krl/set-circular-motion-mode :base) ; $CIRC_TYPE = #BASE
        (krl/set-swivel-velocity 200) ; $VEL.ORI1 = 200
        (krl/set-rotational-velocity 200) ; $VEL.ORI2 = 200
        (krl/set-swivel-acceleration 100); $ACC.ORI1 = 100
        (krl/set-rotational-acceleration 100) ; $ACC.ORI2 = 100
        (krl/set-center-point-velocity 3) ; $VEL.CP=3
        (krl/set-center-point-acceleration 10) ; $ACC.CP=10
        (krl/set-lookahead 0) ; $ADVANCE = 0
        (krl/set-approximation-range 0) ; $APO.CPTP = 0
        (krl/set-distance-criterion 0) ; $APO.CDIS = 0

        (krl/set-base-frame {:x 0 :y 0 :z 0 :a 0 :b 0 :c 180})
        (krl/set-tool-frame {:x 0 :y 0 :z 0 :a 0 :b 0 :c 180})

        (def COM_ROUNDM 1)
        (def COM_ACTION 1)
        (def COM_ACTCNT 0)

        (while (>= COM_ACTION 0)
          (case COM_ACTION
            1 (do (def COM_ACTCNT "COM_ACTCNT + 1")
                  (def COM_ACTION 0))
            2 (do
                (if (>= COM_ROUNDM 0)
                  (krl/ptp-approximate COM_E6AXIS)
                  (krl/ptp COM_E6AXIS))
                (def COM_ACTION 0))
            3 (do
                (if (>= COM_ROUNDM 0)
                  (krl/lin-approximate COM_FRAME)
                  (krl/lin COM_E6AXIS))
                (def COM_ACTION 0))
            4 (do
                (if (>= COM_ROUNDM 0)
                  (krl/circle-approximate COM_POS COM_FRAME)
                  (krl/circle COM_POS COM_FRAME))
                (def COM_ACTION 0))
            5 (do
                (def COM_ACTCNT "COM_ACTCNT + 1")
                (krl/set-tool-frame COM_FRAME)
                (def COM_ACTION 0))
            6 (do
                (def COM_ACTCNT "COM_ACTCNT + 1")
                (krl/set-center-point-velocity COM_VALUE1)
                (def COM_ACTION 0))
            7 (do
                (def COM_ACTCNT "COM_ACTCNT + 1")
                (if (> COM_VALUE1 0)
                  (krl/set-center-point-velocity COM_VALUE1))
                (if (> COM_VALUE2 0)
                  (krl/for-loop "joint_id" 1 6
                    (if (> COM_VALUE2 100)
                      (krl/set-axis-velocity "joint_id" 100)
                      (krl/set-axis-velocity "joint_id" COM_VALUE2))))
                (if (> COM_VALUE3 0)
                  (krl/set-center-point-acceleration COM_VALUE3))
                (if (> COM_VALUE4 0)
                  (krl/for-loop "joint_id" 1 6
                    (if (> COM_VALUE4 100)
                      (krl/set-axis-acceleration "joint_id" 100)
                      (krl/set-axis-acceleration "joint_id" COM_VALUE4))))
                (def COM_ACTION 0))
            8 (do
                (def COM_ACTCNT "COM_ACTCNT + 1")
                (if (>= COM_ROUNDM 0)
                  (if (> COM_ROUNDM 100)
                    (krl/set-approximation-range 100)
                    (krl/set-approximation-range COM_ROUNDM))
                  (do
                    (krl/set-approximation-range 0)
                    (krl/set-distance-criterion 0)
                    (krl/set-lookahead 0)))
                (def COM_ACTION 0))
            9 (do
                (def COM_ACTCNT "COM_ACTCNT + 1")
                (krl/wait COM_VALUE1)
                (def COM_ACTION 0))
            10 (do
                 (def COM_ACTCNT "COM_ACTCNT + 1")
                 ; TODO handle setting I/O
                 (def COM_ACTION 0))
            11 (do
                 (def COM_ACTCNT "COM_ACTCNT + 1")
                 (krl/ptp COM_E6AXIS)
                 (krl/lin COM_FRAME)
                 (krl/sync-lookahead) ; WAIT 0
                 (def COM_ACTION 0))
            12 (do
                 (def COM_ACTCNT "COM_ACTCNT + 1")
                 ; TODO wait on I/O
                 (def COM_ACTION 0))
            13 (do
                 (def COM_ACTCNT "COM_ACTCNT + 1")
                 ; TODO trigger programs
                 (def COM_VALUE1 0)
                 (def COM_ACTION 0))))))))


