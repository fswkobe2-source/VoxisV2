-- Must match `constants.home_coord` (org.rsmod.api.config.Constants): 3087, 3496, level 0.
UPDATE realms
SET
    spawn_coord = '0_48_54_15_40',
    respawn_coord = '0_48_54_15_40'
WHERE name = 'dev';
