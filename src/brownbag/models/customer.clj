(ns brownbag.models.customer)

(def customers (atom [{:id 1 :name "Chris"}]))

(defn get-customer-model [id]
  (first (filter #(= (% :id) id) @customers)))

(defn add-customer [customer]
  (swap! customers conj customer))
