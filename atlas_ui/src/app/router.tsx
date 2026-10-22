import { createBrowserRouter } from 'react-router';
import { LoginPage, RegisterPage, RequireAuth } from '@/features/auth';
import {
  MovementDetailsPage,
  MovementEditorPage,
  MovementIndexPage,
} from '@/features/movements';
import {
  RoutineDetailsPage,
  RoutineEditorPage,
  RoutineLibraryPage,
} from '@/features/routines';
import { AppLayout } from './layout/AppLayout';
import { IllustrationSettingsPage } from './pages/IllustrationSettingsPage';
import { LandingPage } from './pages/LandingPage';
import { NotFoundPage } from './pages/NotFoundPage';

export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [
      { index: true, element: <LandingPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },

      // Public
      { path: 'movements', element: <MovementIndexPage /> },
      { path: 'movements/:key', element: <MovementDetailsPage /> },

      // Maintainers
      {
        element: <RequireAuth role="MAINTAINER" />,
        children: [
          { path: 'movements/new', element: <MovementEditorPage /> },
          { path: 'movements/:key/edit', element: <MovementEditorPage /> },
          {
            path: 'settings/illustrations',
            element: <IllustrationSettingsPage />,
          },
        ],
      },

      // Signed-in users
      {
        element: <RequireAuth />,
        children: [
          { path: 'routines', element: <RoutineLibraryPage /> },
          { path: 'routines/new', element: <RoutineEditorPage /> },
          { path: 'routines/:id', element: <RoutineDetailsPage /> },
          { path: 'routines/:id/edit', element: <RoutineEditorPage /> },
        ],
      },

      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
