import { createFileRoute, Link } from "@tanstack/react-router";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Eye, EyeOff, LogIn } from "lucide-react";
import { toast } from "sonner";

import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";

import { authApi } from "@/lib/api/authApi";
import { useAuth } from "@/lib/auth/context";
import { USE_MOCK_DATA } from "@/lib/env";
import { extractErrorMessage } from "@/lib/api/errors";

const schema = z.object({
  email: z.string().trim().email("Enter a valid email address"),
  password: z.string().min(1, "Password is required"),
  remember: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

export const Route = createFileRoute("/login")({
  ssr: false,
  component: LoginPage,
});

function LoginPage() {
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: "",
      password: "",
      remember: false,
    },
  });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      authApi.login({
        email: values.email.trim(),
        password: values.password,
      }),

    onSuccess: (tokens) => {
      console.log("Login successful", tokens);

      login(tokens);
      toast.success("Login successful");

      // A full navigation ensures AuthProvider reads the saved token.
      window.location.href = "/dashboard";
    },

    onError: (error) => {
      console.error("Login failed", error);
      toast.error(extractErrorMessage(error));
    },
  });

  const onSubmit = (values: FormValues) => {
    console.log("Submitting login form", values);
    mutation.mutate(values);
  };

  const onInvalid = () => {
    console.log("Login validation errors", errors);
    toast.error("Please enter a valid email and password.");
  };

  return (
    <AuthShell
      title="Sign in to NeuroForge"
      subtitle="Enter your credentials to access your workspace."
      footer={
        <>
          Don't have an account?{" "}
          <Link
            to="/register"
            className="font-medium text-primary hover:underline"
          >
            Create one
          </Link>
        </>
      }
    >
      <form
        className="space-y-4"
        onSubmit={handleSubmit(onSubmit, onInvalid)}
        noValidate
      >
        <div className="space-y-1.5">
          <Label htmlFor="email">Email</Label>

          <Input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="super@neuroforge.dev"
            {...register("email")}
          />

          {errors.email && (
            <p className="text-xs text-destructive">
              {errors.email.message}
            </p>
          )}
        </div>

        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <Label htmlFor="password">Password</Label>

            <Link
              to="/forgot-password"
              className="text-xs text-muted-foreground hover:text-foreground"
            >
              Forgot password?
            </Link>
          </div>

          <div className="relative">
            <Input
              id="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              placeholder="Password@123"
              {...register("password")}
            />

            <button
              type="button"
              onClick={() => setShowPassword((value) => !value)}
              className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-muted-foreground hover:text-foreground"
              aria-label={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? (
                <EyeOff className="size-4" />
              ) : (
                <Eye className="size-4" />
              )}
            </button>
          </div>

          {errors.password && (
            <p className="text-xs text-destructive">
              {errors.password.message}
            </p>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Controller
            name="remember"
            control={control}
            render={({ field }) => (
              <Checkbox
                id="remember"
                checked={field.value}
                onCheckedChange={(checked) =>
                  field.onChange(checked === true)
                }
              />
            )}
          />

          <Label
            htmlFor="remember"
            className="text-sm font-normal"
          >
            Remember me
          </Label>
        </div>

        <Button
          type="submit"
          className="w-full"
          disabled={mutation.isPending}
        >
          <LogIn className="mr-2 size-4" />

          {mutation.isPending ? "Signing in..." : "Sign In"}
        </Button>

        <Button
          type="button"
          variant="outline"
          className="w-full"
          onClick={() => {
            window.location.href = authApi.googleLoginUrl();
          }}
        >
          Continue with Google
        </Button>
      </form>

      {USE_MOCK_DATA && (
        <div className="mt-6 rounded-lg border border-dashed bg-muted/40 p-3 text-xs text-muted-foreground">
          <div className="mb-1 font-medium text-foreground">
            Demo credentials
          </div>

          <p>super@neuroforge.dev</p>
          <p>Password@123</p>
        </div>
      )}
    </AuthShell>
  );
}