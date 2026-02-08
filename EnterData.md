1 - docker cp export.geojson postgres:/tmp/

2 - docker exec postgres ogr2ogr -f "PostgreSQL" PG:"dbname=roads_db user=postgres" "/tmp/export.geojson" -nln villes_mada -nlt PROMOTE_TO_MULTI -overwrite

3 - docker exec postgres rm /tmp/export.geojson
