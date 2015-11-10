(ns lcmap-rest.api.routes
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [compojure.route :as route]
            [lcmap-client.core]
            [lcmap-client.ccdc]
            [lcmap-client.ccdc.job]
            [lcmap-client.ccdc.model]
            [lcmap-client.lcmap]
            [lcmap-client.sample]
            [lcmap-client.sample.job]
            [lcmap-client.sample.model]
            [lcmap-client.l8.surface-reflectance]
            [lcmap-rest.api.ccdc :as ccdc]
            [lcmap-rest.api.sample :as sample]
            [lcmap-rest.api.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.api.management :as management]
            [lcmap-rest.util :as util]))

(defroutes ccdc-science-model
  (context lcmap-client.ccdc.model/context []
    (GET "/" request
      (ccdc/get-model-resources (:uri request)))
    (POST "/" [arg1 arg2 :as request]
      (ccdc/run-model arg1 arg2))
    (GET "/:job-id" [job-id]
      (ccdc/get-job-result [job-id]))
    (POST "/run/:job-id" [job-id]
      (ccdc/run-model job-id :arg1 :arg2))))

(defroutes ccdc-job-management
  (context lcmap-client.ccdc.job/context []
    (GET "/" request
      (ccdc/get-job-resources (:uri request)))
    (POST "/" [arg1 arg2 :as request]
      (ccdc/create-job arg1 arg2))
    (GET "/:job-id" [job-id]
      (ccdc/get-job-result job-id))
    (PUT "/:job-id" [job-id]
      (ccdc/update-job job-id))
    (HEAD "/:job-id" [job-id]
      (ccdc/get-info job-id))
    (POST "/run/:job-id" [job-id]
      (ccdc/run-model job-id :arg1 :arg2))))

(defroutes ccdc-routes
  (context lcmap-client.ccdc/context []
    (GET "/" request
      (ccdc/get-resources (:uri request))))
  ccdc-science-model
  ccdc-job-management)

(defroutes sample-science-model
  (context lcmap-client.sample.model/context []
    (GET "/" request
      (sample/get-model-resources (:uri request)))
    (POST "/" [seconds year :as request]
      (sample/run-model seconds year))
    (GET "/:job-id" [job-id]
      (sample/get-job-result job-id))))

(defroutes sample-job-management
  (context lcmap-client.sample.job/context []
    (GET "/" request
      (sample/get-job-resources (:uri request)))
    (GET "/:job-id" [job-id]
      (sample/get-job-result job-id))
    (PUT "/:job-id" [job-id]
      (sample/update-job job-id))
    (HEAD "/:job-id" [job-id]
      (sample/get-info job-id))
    (GET "/status/:job-id" [job-id]
      (sample/get-job-status job-id))))

(defroutes sample-routes
  (context lcmap-client.sample/context []
    (GET "/" request
      (sample/get-resources (:uri request))))
  sample-science-model
  sample-job-management)

(defroutes surface-reflectance-routes
  (context lcmap-client.l8.surface-reflectance/context []
    (GET "/" request
      (l8-sr/get-resources (:uri request)))
    (GET "/tiles" [point extent time band :as request]
      (l8-sr/get-tiles point extent time band request))
    (GET "/rod" [point time band :as request]
      (l8-sr/get-rod point time band request))))

;; XXX this needs to go into a protected area; see this ticket:
;;  https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes management-routes
  (context (str lcmap-client.lcmap/context "/manage") []
    (GET "/status" [] (management/get-status))
    ;; XXX add more management resources
    ))

(defroutes v0
  sample-routes
  ccdc-routes
  surface-reflectance-routes
  management-routes
  (route/not-found "Resource not found"))

(defroutes v1
  management-routes
  (route/not-found "Resource not found"))

(defroutes v2
  management-routes
  (route/not-found "Resource not found"))
