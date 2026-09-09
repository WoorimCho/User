-- Seed the advisory catalogue of common dietary restrictions. Codes are
-- namespaced like catalogue tags; `kind` is the namespace. Ids are fixed because
-- this table is seed-only (no create endpoint); the sequence is bumped past
-- them in case one is added later.

insert into restriction (id, code, label, kind, description) values
  (1,  'diet:vegetarian',      'Vegetarian',        'diet',      'No meat, poultry or seafood.'),
  (2,  'diet:vegan',           'Vegan',             'diet',      'No animal products at all.'),
  (3,  'diet:pescatarian',     'Pescatarian',       'diet',      'Vegetarian plus seafood.'),
  (4,  'diet:keto',            'Keto',              'diet',      'Very low carbohydrate.'),
  (5,  'diet:paleo',           'Paleo',             'diet',      'Whole foods; no grains, legumes or dairy.'),
  (6,  'diet:low-sodium',      'Low sodium',        'diet',      'Reduced salt.'),
  (10, 'allergen:peanut',      'Peanut-free',       'allergen',  'Avoids peanuts.'),
  (11, 'allergen:tree-nut',    'Tree-nut-free',     'allergen',  'Avoids almonds, walnuts, cashews, etc.'),
  (12, 'allergen:dairy',       'Dairy-free',        'allergen',  'Avoids milk and milk products.'),
  (13, 'allergen:egg',         'Egg-free',          'allergen',  'Avoids eggs.'),
  (14, 'allergen:gluten',      'Gluten-free',       'allergen',  'Avoids wheat, barley and rye.'),
  (15, 'allergen:soy',         'Soy-free',          'allergen',  'Avoids soybeans and soy products.'),
  (16, 'allergen:shellfish',   'Shellfish-free',    'allergen',  'Avoids crustaceans and molluscs.'),
  (17, 'allergen:fish',        'Fish-free',         'allergen',  'Avoids finned fish.'),
  (18, 'allergen:sesame',      'Sesame-free',       'allergen',  'Avoids sesame seeds and oil.'),
  (20, 'religious:halal',      'Halal',             'religious', 'Permissible under Islamic dietary law.'),
  (21, 'religious:kosher',     'Kosher',            'religious', 'Prepared per Jewish dietary law.'),
  (22, 'religious:jain',       'Jain',              'religious', 'Strict vegetarian; no root vegetables.'),
  (23, 'religious:hindu-veg',  'Hindu vegetarian',  'religious', 'No meat; often no egg.'),
  (30, 'lifestyle:no-alcohol',      'No alcohol',        'lifestyle', 'No alcohol, including in cooking.'),
  (31, 'lifestyle:no-added-sugar',  'No added sugar',    'lifestyle', 'No refined or added sugars.'),
  (32, 'lifestyle:organic-only',    'Organic only',      'lifestyle', 'Only certified-organic ingredients.');

update restriction_seq set next_val = 100;
