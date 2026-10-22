-- System data: the starting Avatar and the standard Viewpoints, so a fresh install can generate
-- Illustrations right away.
INSERT INTO avatar (id, name, specification) VALUES (
    '00000000-0000-4000-8000-000000000001',
    'Default',
    'Style
- Draw in a realistic anatomical sketch style.
- Use black graphite outlines with fine cross-hatching.
- Use only black, white, and grayscale.

Physique
- Depict a very fit, athletic human figure.
- Define muscles clearly with moderate muscularity.
- Maintain realistic anatomy and balanced proportions.
- Avoid exaggerated muscle size and excessive vascularity.

Rendering
- Use consistent line weight and restrained shading.
- Show the entire body, including hands and feet.
- Use a pure white background.

Defaults
- Unless explicitly specified otherwise, exclude props, scenery, and text.
- Unless explicitly specified otherwise, do not use color, photorealism, or 3D rendering.'
);

-- Viewpoints are named after the standard view and angle terms. Three-quarter is the default. Angles around the figure are
-- measured from the front; the three-quarter views use a camera slightly above eye level.
INSERT INTO viewpoint (id, name, specification, is_default) VALUES
    ('00000000-0000-4000-8000-000000000002',
     'Three-quarter',
     'Three-quarter view: the figure is turned 45 degrees away from the camera, showing the front and one side. The camera is slightly above eye level, looking slightly down. The full body is in frame and centered.', TRUE),
    ('00000000-0000-4000-8000-000000000003',
     'Front',
     'Straight-on front view: the figure faces the camera, which is at eye level. The full body is in frame and centered.', FALSE),
    ('00000000-0000-4000-8000-000000000004',
     'Side',
     'Straight side view (profile): the figure is turned 90 degrees to the camera, which is at eye level. The full body is in frame and centered.', FALSE),
    ('00000000-0000-4000-8000-000000000005',
     'Top',
     'Top-down view: the camera is directly above the figure, looking straight down. The full body is in frame and centered.', FALSE),
    ('00000000-0000-4000-8000-000000000006',
     'Back',
     'Straight back view: the figure faces directly away from the camera, which is at eye level. The full body is in frame and centered.', FALSE),
    ('00000000-0000-4000-8000-000000000007',
     'Rear three-quarter',
     'Rear three-quarter view: the figure is turned 135 degrees away from the camera, showing the back and one side. The camera is slightly above eye level, looking slightly down. The full body is in frame and centered.', FALSE);
