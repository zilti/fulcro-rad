(ns com.example.client
  (:require
   [com.example.ui :as ui :refer [Root]]
   [com.example.ui.login-dialog :refer [LoginForm]]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.networking.http-remote :as http]
   [com.fulcrologic.rad.controller :as controller]
   [com.fulcrologic.rad.schema :as schema]
   [com.example.schema :refer [latest-schema]]
   [com.fulcrologic.rad.authorization :as auth]
   [com.fulcrologic.fulcro.ui-state-machines :as uism]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.data-fetch :as df]))

(defn- eql-transform [ast]
  (df/elide-ast-nodes
   ast (fn [k]
         (or
          (= k '[:com.fulcrologic.fulcro.ui-state-machines/asm-id _])
          (= k df/marker-table)
          (= k ::fs/config)
          (= "ui" (when (keyword? k) (namespace k))ns)))))

;; TODO: Constructor function. Allow option to completely autogenerate
;; forms if desired.
(defonce app
  (app/fulcro-app
   {:remotes {:remote (http/fulcro-http-remote {})}
    :global-eql-transform #'eql-transform
    :client-did-mount
    (fn [app]
      (auth/start! app {:local (-> LoginForm
                                   (comp/get-ident {})
                                   (uism/with-actor-class LoginForm))})
      (controller/start! app
                         {::schema/schema        latest-schema
                          ::controller/home-page ["landing-page"]
                          ::controller/router    ui/MainRouter
                          ::controller/id        :main-controller}))}))

(defn start [] (app/mount! app Root "app"))
