import { createBrowserRouter } from 'react-router';
import { AppLayout } from '../layout/AppLayout';
import { ComingSoonPage } from '../pages/ComingSoonPage';
import { LandingPage } from '../pages/LandingPage';
import { NotFoundPage } from '../pages/NotFoundPage';

export const router = createBrowserRouter(
  [
    {
      element: <AppLayout />,
      children: [
        { index: true, element: <LandingPage /> },
        { path: 'movements', element: <ComingSoonPage title="Movements" /> },
        { path: 'routines', element: <ComingSoonPage title="Routines" /> },
        { path: '*', element: <NotFoundPage /> },
      ],
    },
  ],
  { basename: import.meta.env.BASE_URL.replace(/\/$/, '') || '/' },
);
