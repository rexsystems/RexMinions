const root = "rexminions";

export const rexminions = {
  label: "RexMinions",
  items: [
    { label: "Overview", slug: root },
    {
      label: "Getting started",
      items: [
        { label: "Installation", slug: `${root}/getting-started/installation` },
        { label: "Requirements", slug: `${root}/getting-started/requirements` },
      ],
    },
    {
      label: "Configuration",
      items: [
        { label: "Settings", slug: `${root}/configuration/overview` },
        { label: "Minion types", slug: `${root}/configuration/minion-types` },
        { label: "Messages", slug: `${root}/configuration/messages` },
        { label: "Prices", slug: `${root}/configuration/prices` },
      ],
    },
    {
      label: "Guides",
      items: [
        { label: "Levels and limits", slug: `${root}/guides/levels-and-limits` },
      ],
    },
    {
      label: "Reference",
      items: [
        { label: "Commands", slug: `${root}/reference/commands` },
        { label: "Permissions", slug: `${root}/reference/permissions` },
        { label: "Placeholders", slug: `${root}/reference/placeholders` },
      ],
    },
    { label: "FAQ", slug: `${root}/faq` },
  ],
};
