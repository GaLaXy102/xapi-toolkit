package de.tudresden.inf.verdatas.xapitools.dave;

import de.tudresden.inf.verdatas.xapitools.dave.persistence.*;
import org.springframework.stereotype.Component;

@Component
public class DaveVisSeeder {
    private final DaveVisRepository daveVisRepository;
    private final DaveQueryRepository daveQueryRepository;
    private final DaveGraphDescriptionRepository daveGraphDescriptionRepository;

    /**
     * This class is instantiated by Spring Boot and not intended for manual creation.
     */
    DaveVisSeeder(DaveVisRepository daveVisRepository, DaveQueryRepository daveQueryRepository, DaveGraphDescriptionRepository daveGraphDescriptionRepository) {
        this.daveVisRepository = daveVisRepository;
        this.daveQueryRepository = daveQueryRepository;
        this.daveGraphDescriptionRepository = daveGraphDescriptionRepository;
        this.seed();
    }

    /**
     * Create some connection samples
     */
    private void seed() {
        try {
            if (this.daveQueryRepository.count() == 0) {
                this.daveQueryRepository.save(new DaveQuery("Learners' activity",
                        """
                                [:find (count ?s) ?c
                                  :where
                                  [?s :statement/actor ?a]
                                  [?a :agent/name ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' activity [VerDatAs]",
                        """
                                [:find (count ?s) ?c
                                  :where
                                  [?s :statement/actor ?a]
                                  [?a :agent.account/name ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' activity per Activity",
                        """
                                [:find (count ?s) ?c
                                  :where
                                  [?s :statement/object ?o]
                                  [?o :activity/id ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners per Activity",
                        """
                                [:find (count-distinct ?ac) ?c
                                  :where
                                  [?s :statement/object ?o]
                                  [?o :activity/id ?c]
                                  [?s :statement/actor ?a]
                                  [?a :agent/name ?ac]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners per Activity [VerDatAs]",
                        """
                                [:find (count-distinct ?ac) ?c
                                  :where
                                  [?s :statement/object ?o]
                                  [?o :activity/id ?c]
                                  [?s :statement/actor ?a]
                                  [?a :agent.account/name ?ac]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' task determination",
                        """
                                [:find (count-distinct ?ac) ?c
                                  :where
                                  [?a :agent/name ?c]
                                  [?ac :activity/id ?ac-id]
                                  [?s-suc :statement/actor ?a]
                                  [?s-suc :statement/object ?ac]
                                  [?s-suc :statement.result/success true]
                                  [?s-suc :statement/timestamp-inst ?s-suc-t]
                                  ;; fail
                                  [?s-f0 :statement/actor ?a]
                                  [?s-f0 :statement/object ?ac]
                                  [?s-f0 :statement.result/success false]
                                  [?s-f0 :statement/timestamp-inst ?s-f0-t]
                                  [(< ?s-f0-t ?s-suc-t)]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' task determination [VerDatAs]",
                        """
                                [:find (count-distinct ?ac) ?c
                                  :where
                                  [?a :agent.account/name ?c]
                                  [?ac :activity/id ?ac-id]
                                  [?s-suc :statement/actor ?a]
                                  [?s-suc :statement/object ?ac]
                                  [?s-suc :statement.result/success true]
                                  [?s-suc :statement/timestamp-inst ?s-suc-t]
                                  ;; fail
                                  [?s-f0 :statement/actor ?a]
                                  [?s-f0 :statement/object ?ac]
                                  [?s-f0 :statement.result/success false]
                                  [?s-f0 :statement/timestamp-inst ?s-f0-t]
                                  [(< ?s-f0-t ?s-suc-t)]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' unlearning",
                        """
                                [:find (count-distinct ?ac) ?c
                                  :where
                                  [?a :agent/name ?c]
                                  [?ac :activity/id ?ac-id]
                                  [?s-suc :statement/actor ?a]
                                  [?s-suc :statement/object ?ac]
                                  [?s-suc :statement.result/success true]
                                  [?s-suc :statement/timestamp-inst ?s-suc-t]
                                  ;; fail
                                  [?s-f0 :statement/actor ?a]
                                  [?s-f0 :statement/object ?ac]
                                  [?s-f0 :statement.result/success false]
                                  [?s-f0 :statement/timestamp-inst ?s-f0-t]
                                  [(> ?s-f0-t ?s-suc-t)]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' unlearning [VerDatAs]",
                        """
                                [:find (count-distinct ?ac) ?c
                                  :where
                                  [?a :agent.account/name ?c]
                                  [?ac :activity/id ?ac-id]
                                  [?s-suc :statement/actor ?a]
                                  [?s-suc :statement/object ?ac]
                                  [?s-suc :statement.result/success true]
                                  [?s-suc :statement/timestamp-inst ?s-suc-t]
                                  ;; fail
                                  [?s-f0 :statement/actor ?a]
                                  [?s-f0 :statement/object ?ac]
                                  [?s-f0 :statement.result/success false]
                                  [?s-f0 :statement/timestamp-inst ?s-f0-t]
                                  [(> ?s-f0-t ?s-suc-t)]]"""));
                this.daveQueryRepository.save(new DaveQuery("Successful execution per Activity",
                        """
                                [:find (count ?v) ?c
                                  :where
                                  [?s :statement/object ?o]
                                  [?s :statement.result/success true ?v]
                                  [?o :activity/id ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Unsuccessful execution per Activity",
                        """
                                [:find (count ?v) ?c
                                  :where
                                  [?s :statement/object ?o]
                                  [?s :statement.result/success false ?v]
                                  [?o :activity/id ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Successful learners per Activity",
                        """
                                [:find (count-distinct ?ac) ?c
                                 :where
                                 [?s :statement/object ?o]
                                 [?s :statement.result/success true]
                                 [?o :activity/id ?c]
                                 [?s :statement/actor ?l]
                                 [?l :agent/name ?ac]]"""));
                this.daveQueryRepository.save(new DaveQuery("Successful learners per Activity [VerDatAs]",
                        """
                                [:find (count-distinct ?ac) ?c
                                 :where
                                 [?s :statement/object ?o]
                                 [?s :statement.result/success true]
                                 [?o :activity/id ?c]
                                 [?s :statement/actor ?l]
                                 [?l :agent.account/name ?ac]]"""));
                this.daveQueryRepository.save(new DaveQuery("Unsuccessful learners per Activity",
                        """
                                [:find (count-distinct ?ac) ?c
                                 :where
                                 [?s :statement/object ?o]
                                 [?s :statement.result/success false]
                                 [?o :activity/id ?c]
                                 [?s :statement/actor ?l]
                                 [?l :agent/name ?ac]]"""));
                this.daveQueryRepository.save(new DaveQuery("Unsuccessful learners per Activity [VerDatAs]",
                        """
                                [:find (count-distinct ?ac) ?c
                                 :where
                                 [?s :statement/object ?o]
                                 [?s :statement.result/success false]
                                 [?o :activity/id ?c]
                                 [?s :statement/actor ?l]
                                 [?l :agent.account/name ?ac]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' scaled scores over time [VerDatAs]",
                        """
                                [:find ?x ?y ?c
                                  :where
                                  [?s :statement/timestamp ?t]
                                  [?s :statement.result.score/scaled ?y]
                                  [?s :statement/actor ?a]
                                  [?a :agent/name ?c]
                                  [->unix ?t ?x]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' success over time [DATASIM]",
                        """
                                [:find ?x ?y ?c
                                  :where
                                  [?s :statement/timestamp ?t]
                                  [?s :statement.result/success ?y]
                                  [?s :statement/actor ?a]
                                  [?a :agent/name ?c]
                                  [->unix ?t ?x]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' raw score per Activity [VerDatAs]",
                        """
                                [:find ?v ?c
                                  :where
                                  [?s :statement/object ?ac]
                                  [?s :statement.result.score/raw ?v]
                                  [?s :statement/actor ?a]
                                  [?a :agent/name ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Learners' average scaled score [VerDatAs]",
                        """
                                [:find (avg ?v) ?c
                                  :where
                                  [?s :statement/object ?ac]
                                  [?s :statement.result.score/scaled ?v]
                                  [?s :statement/actor ?a]
                                  [?a :agent/name ?c]]"""));
                this.daveQueryRepository.save(new DaveQuery("Linear regression of learners' scaled scores by hour of day [VerDatAs]",
                        """
                                [:find ?h ?y
                                  :where
                                  [?s :statement/timestamp ?t]
                                  [time-format "H" ?t ?h]
                                  [?s :statement.result.score/scaled ?y]]"""));
            }
            if (this.daveGraphDescriptionRepository.count() == 0) {
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 statements DESC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?s", "order" : "descending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?s", "order": "descending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?s"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?s"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 statements ASC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?s", "order" : "ascending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?s", "order": "ascending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?s"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?s"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 distinct DESC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count-distinct_?ac", "order" : "descending" }},
                                        {
                                          "type": "window",
                                          "sort": {"field": "count-distinct_?ac", "order": "descending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count-distinct_?ac"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale", "tickMinStep":1 }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count-distinct_?ac"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 distinct ASC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count-distinct_?ac", "order" : "ascending" }},
                                        {
                                          "type": "window",
                                          "sort": {"field": "count-distinct_?ac", "order": "ascending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count-distinct_?ac"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale", "tickMinStep":1 }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count-distinct_?ac"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 value DESC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?v", "order" : "descending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?v", "order": "descending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?v"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?v"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 value ASC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?v", "order" : "ascending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?v", "order": "ascending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?v"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?v"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 count value DESC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?v", "order" : "descending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?v", "order": "descending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?v"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?v"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 count value ASC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?v", "order" : "ascending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?v", "order": "ascending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?v"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?v"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 average value DESC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "avg_?v", "order": "descending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "avg_?v", "order": "descending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],
                                  
                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "avg_?v"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "avg_?v"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Top 10 average value ASC",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 20,

                                  "data": [
                                    {
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "avg_?v", "order": "ascending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "avg_?v", "order": "ascending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],
                                  
                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "avg_?v"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "avg_?v"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Scatter Plot",
                        """
                                {
                                  "legends": [
                                    {
                                      "fill": "color"
                                    }
                                  ],
                                  "axes": [
                                    {
                                      "orient": "bottom",
                                      "scale": "x",
                                      "labelAngle": 60,
                                      "labelAlign": "left",
                                      "labelLimit": 112,
                                      "labelOverlap": true,
                                      "labelSeparation": -35
                                    },
                                    {
                                      "orient": "left",
                                      "scale": "y"
                                    }
                                  ],
                                  "width": 500,
                                  "scales": [
                                    {
                                      "name": "x",
                                      "type": "time",
                                      "range": "width",
                                      "domain": {
                                        "data": "result",
                                        "field": "?x"
                                      }
                                    },
                                    {
                                      "name": "y",
                                      "type": "linear",
                                      "range": "height",
                                      "nice": true,
                                      "zero": true,
                                      "domain": {
                                        "data": "result",
                                        "field": "?y"
                                      }
                                    },
                                    {
                                      "name": "color",
                                      "type": "ordinal",
                                      "range": "category",
                                      "domain": {
                                        "data": "result",
                                        "field": "?c"
                                      }
                                    }
                                  ],
                                  "padding": 20,
                                  "marks": [
                                    {
                                      "type": "group",
                                      "from": {
                                        "facet": {
                                          "name": "series",
                                          "data": "result",
                                          "groupby": "?c"
                                        }
                                      },
                                      "marks": [
                                        {
                                          "type": "symbol",
                                          "from": {
                                            "data": "series"
                                          },
                                          "encode": {
                                            "enter": {
                                              "size": {
                                                "value": 50
                                              },
                                              "x": {
                                                "scale": "x",
                                                "field": "?x"
                                              },
                                              "y": {
                                                "scale": "y",
                                                "field": "?y"
                                              },
                                              "fill": {
                                                "scale": "color",
                                                "field": "?c"
                                              }
                                            }
                                          }
                                        }
                                      ]
                                    }
                                  ],
                                  "$schema": "https://vega.github.io/schema/vega/v4.json",
                                  "signals": [
                                    {
                                      "name": "interpolate",
                                      "value": "linear"
                                    }
                                  ],
                                  "height": 200
                                }
                                """));
                this.daveGraphDescriptionRepository.save(new DaveGraphDescription("Linear Regression by hour of day",
                        """
                                {
                                  "data": [
                                    {
                                      "name": "cleaned",
                                      "source": "result",
                                      "transform": [
                                        {"type": "project", "fields": ["?h", "?y"], "as": ["hourString", "score"]},
                                        { "type": "formula", "as": "hour", "expr": "toNumber(datum.hourString)"}
                                      ]
                                    },
                                    {
                                      "name": "trend",
                                      "source": "cleaned",
                                      "transform": [
                                        {
                                          "type": "regression",
                                          "method": "linear",
                                          "order": 3,
                                          "extent": [0, 23],
                                          "x": "hour",
                                          "y": "score",
                                          "as": ["u", "v"]
                                        }
                                      ]
                                    }
                                    ],
                                  "axes": [
                                    {
                                      "orient": "bottom",
                                      "scale": "x",
                                      "labelAngle": 60,
                                      "labelAlign": "left"
                                    },
                                    {
                                      "orient": "left",
                                      "scale": "y"
                                    }
                                  ],
                                  "width": 500,
                                  "scales": [
                                    {
                                      "name": "x",
                                      "range": "width",
                                      "domain": {
                                        "data": "cleaned",
                                        "field": "hour"
                                      }
                                    },
                                    {
                                      "name": "y",
                                      "type": "linear",
                                      "range": "height",
                                      "nice": true,
                                      "zero": true,
                                      "domain": {
                                        "data": "cleaned",
                                        "field": "score"
                                      }
                                    },
                                    {
                                      "name": "color",
                                      "type": "ordinal",
                                      "range": "category",
                                      "domain": {
                                        "data": "cleaned",
                                        "field": "?c"
                                      }
                                    }
                                  ],
                                  "padding": 20,
                                  "marks": [
                                    {
                                      "type": "group",
                                      "from": {
                                        "facet": {
                                          "name": "series",
                                          "data": "cleaned",
                                          "groupby": "?c"
                                        }
                                      },
                                      "marks": [
                                        {
                                          "type": "symbol",
                                          "from": {
                                            "data": "series"
                                          },
                                          "encode": {
                                            "enter": {
                                              "size": {
                                                "value": 50
                                              },
                                              "x": {
                                                "scale": "x",
                                                "field": "hour"
                                              },
                                              "y": {
                                                "scale": "y",
                                                "field": "score"
                                              },
                                              "fill": {
                                                "scale": "color",
                                                "field": "?c"
                                              }
                                            }
                                          }
                                        }
                                      ]
                                    },
                                    {
                                      "type": "group",
                                      "from": {
                                        "facet": {
                                          "data": "trend",
                                          "name": "curve",
                                          "groupby": "hour"
                                        }
                                      },
                                      "marks": [
                                        {
                                          "type": "line",
                                          "from": {"data": "curve"},
                                          "encode": {
                                            "enter": {
                                              "x": {"scale": "x", "field": "u"},
                                              "y": {"scale": "y", "field": "v"},
                                              "stroke": {"value": "firebrick"}
                                            }
                                          }
                                        }
                                      ]
                                    }
                                  ],
                                  "$schema": "https://vega.github.io/schema/vega/v4.json",
                                  "signals": [
                                    {
                                      "name": "interpolate",
                                      "value": "linear"
                                    }
                                  ],
                                  "height": 200
                                }"""));
            }
            if (this.daveVisRepository.count() == 0) {
                this.daveVisRepository.save(new DaveVis("Learners' activity",
                        this.daveQueryRepository.findByName("Learners' activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 statements DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' activity [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners' activity [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 statements DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' activity per Activity",
                        this.daveQueryRepository.findByName("Learners' activity per Activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 statements DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners per Activity",
                        this.daveQueryRepository.findByName("Learners per Activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners per Activity [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners per Activity [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' task determination",
                        this.daveQueryRepository.findByName("Learners' task determination").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' task determination [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners' task determination [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' unlearning",
                        this.daveQueryRepository.findByName("Learners' unlearning").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' unlearning [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners' unlearning [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Successful execution per Activity",
                        this.daveQueryRepository.findByName("Successful execution per Activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 count value DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Unsuccessful execution per Activity",
                        this.daveQueryRepository.findByName("Unsuccessful execution per Activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 count value DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Successful learners per Activity",
                        this.daveQueryRepository.findByName("Successful learners per Activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Successful learners per Activity [VerDatAs]",
                        this.daveQueryRepository.findByName("Successful learners per Activity [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Unsuccessful learners per Activity",
                        this.daveQueryRepository.findByName("Unsuccessful learners per Activity").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Unsuccessful learners per Activity [VerDatAs]",
                        this.daveQueryRepository.findByName("Unsuccessful learners per Activity [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 distinct DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' scaled scores over time [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners' scaled scores over time [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Scatter Plot").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' success over time [DATASIM]",
                        this.daveQueryRepository.findByName("Learners' success over time [DATASIM]").get(),
                        this.daveGraphDescriptionRepository.findByName("Scatter Plot").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' raw score per Activity [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners' raw score per Activity [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 value DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Learners' average scaled score [VerDatAs]",
                        this.daveQueryRepository.findByName("Learners' average scaled score [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Top 10 average value DESC").get(), true));
                this.daveVisRepository.save(new DaveVis("Linear regression of learners' scaled scores by hour of day [VerDatAs]",
                        this.daveQueryRepository.findByName("Linear regression of learners' scaled scores by hour of day [VerDatAs]").get(),
                        this.daveGraphDescriptionRepository.findByName("Linear Regression by hour of day").get(), true));
            }
        } catch (Exception ignored) {
        }
    }
}
