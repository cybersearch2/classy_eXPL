resource surface_area_increase axiom (country, surface_area, id) = "agri_20_year";

template increased(country, surface_area, id);

query<axiom> increased_query(surface_area_increase : increased);
