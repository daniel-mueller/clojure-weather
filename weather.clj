(ns forecast
  (:use (clojure [xml :only [parse]] [zip :only [xml-zip]])
        (clojure.contrib duck-streams str-utils pprint)
        (clojure.contrib.zip-filter xml)))

;708287


(defn fetch-xml [uri]
  (xml-zip
   (parse
    (org.xml.sax.InputSource.
     (java.io.StringReader.
      (slurp* (java.net.URI. (re-gsub #"\s+" "+" (str uri)))))))))

(defn yahoo-weather
  ([location]
     (let [rss (fetch-xml (str "http://weather.yahooapis.com/forecastrss?w=" location "&u=c"))]
       (if (= (text (xml1-> rss :channel :item :title)) "City not found")
         "City not found.  Go to http://weather.yahoo.com/, search for your city, and look in the URL for the location code."
         (let [location (xml1-> rss :channel (keyword (str "yweather:" %)))                                           
               conditions (xml1-> rss :channel :item :yweather:condition)
               date (re-find #"\d+:\d+.*" (xml1-> rss :channel :item :pubDate text))
               fors (xml-> rss :channel :item :yweather:forecast)]
           (cl-format true
"In ~a, ~a (~a) beträgt die Temperatur: ~a\u00B0 ~a"
                      (attr loc :city) (attr loc :region) date
                      (attr conditions :temp) (attr units :temperature)
                      ))))))