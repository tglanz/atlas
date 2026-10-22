-- System data: the standard Viewpoints.
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
