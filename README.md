# Smart Campus Navigation

## Campus data and routes

Campus records are stored in the CSV files under `src/main/resources/data` and
loaded into SQLite by `CampusDataLoader`. `pathways.csv` defines the walking
graph, including map waypoint nodes such as `P1` and junctions such as `J2`.
Its two columns are `from_node_id,to_node_id`; these are generic graph nodes,
not necessarily building IDs, because the map contains route points as well as
buildings.

`RouteServices.findBuildingRoute` uses Dijkstra's algorithm to return the
lowest-cost route between two building IDs.
`findRouteBetweenLocations` accepts location IDs from `locations.csv` and
routes between their containing buildings.
The loader uses `jdbc:sqlite:campus.db` by default; override it with the
`campus.db.url` system property if needed.

Each `pathways.csv` row is a directed connection. Reciprocal rows represent
two-way walkways. The CSV has no distance values, so every edge uses a
temporary weight of `1`; routes minimize pathway-edge count, not physical
distance. An optional `distance` column can be added later and Dijkstra will
use those measurements where present. The pathway column labels changed from
`from_building_id,to_building_id` to `from_node_id,to_node_id` because the map
route points are not buildings; the two-column edge-list structure remains.

The pathway rows now follow the diagram's Gate/P/J/B connections. For example,
Gate to Business Building follows Gate, P1, P2, P3, P4, Engineering Grounds,
J2, J3, Business Building.

Run the tests to verify SQLite import/connection, graph direction and weights,
shortest-path behavior, and the diagram routes:

```sh
mvn test
```

Launch the JavaFX screen to try it manually:

```sh
mvn javafx:run
```

The screen reports the SQLite connection status and has current-building and
destination dropdowns populated with campus building names. Select the names
you want, then choose **Test connection and find route**. It displays the
calculated path and labels temporary edge cost as a pathway-edge count, not a
physical distance.