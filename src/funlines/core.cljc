(ns funlines.core
  "A tiny pipeline library that helps untangle a series of steps, where each step
  possibly depends on the output of previous steps, and each result should be
  checked for errors before continuing.

  Pipeline functions can signal an error by returning the result of calling
  `failure`. There is no need to wrap successful values.")

(defn failure
  "Construct a pipeline function failure. Pass in a string message, and optional
  `data`, which will be available under `:funlines.core/error-data`. Even more
  optionally, pass in a map of `props` that will be merged onto the resulting
  map."
  [message & [data props]]
  (assert (or (nil? props) (map? props)) (str "props should be nil or a map, was " (type props)))
  (merge {::failure? true
          ::error-message message}
         (when data
           {::error-data data})
         props))

(defn ok?
  "Given the result of a pipeline function, or indeed a full pipeline run, check
  for success."
  [res]
  (not (::failure? res)))

(defn run-step
  "Run a single step, representing exceptions as pipeline failures."
  [f ctx]
  (try
    (f ctx)
    (catch #?(:clj Throwable :cljs :default) e
      {::failure? true
       ::error-message (.getMessage e)
       ::error-data {:exception e}})))

(defn run
  "Run a pipeline, passing in an optional initial context. A pipeline is a
  sequence of steps, each step a pair of [keyword function]. The function will
  be called with a single argument, a `context` map. The function's return value
  will be assoc-ed to the context map under the keyword from the tuple. The new
  context will be passed to the next pair.

  Returns either the first failing step in a map of
  `{:funlines.core/ok? false, :funlines.core/step step-kw}` and possibly more
  information about the error (see `funlines.core/failure`) or the resulting
  context map after all steps have succeeded."
  [steps & [ctx]]
  (assert (seq steps) "steps is not a collection of steps")
  (assert (every? #(and (sequential? %)
                        (keyword? (first %))
                        (ifn? (second %))) steps)
          "steps is not a collection of keyword/function tuples")
  (assert (or (nil? ctx) (map? ctx)) (str "ctx should be nil or a map, was " (type ctx)))
  (loop [[step+fn & steps] steps
         ctx ctx]
    (if (nil? step+fn)
      ctx
      (let [[step f] step+fn
            res (run-step f ctx)]
        (if (ok? res)
          (recur steps (assoc ctx step res))
          (assoc res ::step step))))))

(defn run-some
  "Runs step functions until a successful result is obtained, then returns it.
  `step-fns` is a sequence of functions. Optionally pass in a `ctx` that will be
  passed to the functions. If no steps are successful then the failure result
  from the final step is returned."
  [step-fns & [ctx]]
  (assert (and (seq step-fns)
               (every? ifn? step-fns)) "step-fns is not a collection of functions")
  (loop [[step-fn & steps] step-fns
         results []]
    (if (nil? step-fn)
      {::failure? true
       ::results results}
      (let [res (run-step step-fn ctx)]
        (if (ok? res)
          res
          (recur steps (conj results res)))))))
